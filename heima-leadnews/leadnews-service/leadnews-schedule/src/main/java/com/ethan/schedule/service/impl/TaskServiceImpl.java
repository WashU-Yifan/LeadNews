package com.ethan.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ethan.schedule.mapper.TaskinfoLogsMapper;
import com.ethan.schedule.mapper.TaskinfoMapper;
import com.ethan.schedule.service.TaskService;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.checkerframework.checker.units.qual.C;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@Slf4j
public class TaskServiceImpl implements TaskService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    public long addTask(Task task) {
        //add task into the db
        if (addTaskToDb(task)) {
            //add task into redis
            addTaskToCache(task);
        }

        return task.getTaskId();
    }

    private void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            //if execution time is no later than now, store it into the list
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        }
        else if(task.getExecuteTime() < calendar.getTimeInMillis()) {
            // else if the execution time is later than now but within 5 min from now, store it into the zSet
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }
    }

    private boolean addTaskToDb(Task task) {
        try {
            Taskinfo taskinfo = new Taskinfo();
            BeanUtils.copyProperties(task, taskinfo);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            task.setTaskId(taskinfo.getTaskId());

            TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
            BeanUtils.copyProperties(task, taskinfoLogs);
            taskinfoLogs.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 删除任务
     * @param taskId
     * @return
     */
    @Override
    public boolean cancelTask(long taskId) {
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);
        if (task != null) {
            removeTaskFromCache(task);
            return true;
        }
        return false;
    }

    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        }
        else {
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }

    }


    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            //删除任务
            taskinfoMapper.deleteById(taskId);

            //更新任务日志
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            taskinfoLogsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().getTime());
        }
        catch (Exception e) {
            log.error("task cancel exception, taskId = {}", taskId);
        }

        return task;
    }

    /**
     * poll task from redis based on the specified type & priority
     * @param type
     * @param priority
     * @return
     */
    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        String key = type +  "_" + priority;
        try {
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(taskJson)) {
                task = JSON.parseObject(taskJson, Task.class);

                //update db
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("poll task exception");
        }

        return task;
    }

    /**
     * redis 定时任务
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
        String token = cacheService.tryLock("SYNC_FUTURE_TASK", 1000 * 30);
        if (StringUtils.isBlank(token)) return;

        log.info("未来数据定时刷新--定时任务");

        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

        for (String futureKey : futureKeys) {
            //
            String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];

            Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

            if (!tasks.isEmpty()) {
                cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
            }
        }
    }

    /**
     * MySQL 定时任务，更新redis缓存
     */
    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData() {

        String token = cacheService.tryLock("RELOAD_FUTURE_TASK", 1000 * 30);
        if (StringUtils.isBlank(token)) return;

        log.info("更新数据到redis--定时任务");
        clearCache();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);;
        List<Taskinfo> taskInfos = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));

        if (taskInfos != null && taskInfos.size() > 0) {
            for (Taskinfo taskinfo : taskInfos) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }

    }

    public void clearCache() {
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }
}
