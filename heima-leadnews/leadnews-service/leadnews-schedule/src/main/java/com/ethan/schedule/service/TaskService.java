package com.ethan.schedule.service;

import com.heima.model.schedule.dtos.Task;

public interface TaskService {

    /**
     * 添加延迟任务
     * @param task
     * @return
     */
    public long addTask(Task task);

    public boolean cancelTask(long taskId);

    /**
     * poll task from redis based on the specified type & priority
     * @param type
     * @param priority
     * @return
     */
    public Task poll(int type, int priority);
}
