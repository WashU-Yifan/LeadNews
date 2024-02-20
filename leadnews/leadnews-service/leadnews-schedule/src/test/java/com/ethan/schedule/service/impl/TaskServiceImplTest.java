package com.ethan.schedule.service.impl;

import com.ethan.schedule.ScheduleApplication;
import com.ethan.schedule.service.TaskService;
import com.heima.model.schedule.dtos.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLOutput;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {

    @Autowired
    private TaskService taskService;
    @Test
    public void addTask() {

        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(50);
        task.setParameters("task test".getBytes());
        task.setExecuteTime(new Date().getTime() + 5000);

        long taskId = taskService.addTask(task);
        System.out.println(taskId);
    }

    @Test
    public void addFutureTask() {
        for (int i = 0; i< 5; ++i) {
            Task task = new Task();
            task.setTaskType(100 + i);
            task.setPriority(50);
            task.setParameters("task test".getBytes());
            task.setExecuteTime(new Date().getTime() + 5000 * i);

            long taskId = taskService.addTask(task);
            System.out.println(taskId);
        }
    }
}