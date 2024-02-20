package com.ethan.schedule.feign;

import com.ethan.apis.schedule.IScheduleClient;
import com.ethan.schedule.service.TaskService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.schedule.dtos.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ScheduleClient implements IScheduleClient {

    @Autowired
    private TaskService taskService;


    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    @Override
    @PostMapping("/api/v1/task/add")
    public ResponseResult addTask(@RequestBody Task task) {
        return ResponseResult.okResult(taskService.addTask(task));
    }

    /**
     * 取消任务
     * @param taskId
     * @return
     */
    @Override
    @GetMapping("/api/v1/task/{taskId}")
    public ResponseResult cancelTask(@PathVariable("taskId") long taskId) {
        return ResponseResult.okResult(taskService.cancelTask(taskId));
    }

    /**
     * 按照优先级与类型拉取任务
     * @param type
     * @param priority
     * @return
     */
    @Override
    @GetMapping("/api/v1/task/{type}/{priority}")
    public ResponseResult poll(@PathVariable("type")int type, @PathVariable("priority")int priority) {
        return ResponseResult.okResult(taskService.poll(type, priority));
    }

}
