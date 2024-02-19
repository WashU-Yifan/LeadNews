package com.ethan.schedule.test;

import com.ethan.schedule.ScheduleApplication;

import com.ethan.schedule.service.TaskService;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class RedisTest {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private TaskService taskService;

    @Test
    public void testList() {
        cacheService.lLeftPush("list_001", "hello, redis1");
        cacheService.lLeftPush("list_001", "hello, redis2");
        cacheService.lLeftPush("list_001", "hello, redis3");
        cacheService.lLeftPush("list_001", "hello, redis4");

        System.out.println(cacheService.lRightPop("list_001"));
        System.out.println(cacheService.lRightPop("list_001"));
        System.out.println(cacheService.lRightPop("list_001"));
        System.out.println(cacheService.lRightPop("list_001"));

    }

    @Test
    public void testZSet() {
        cacheService.zAdd("zSet_001", "hello, redis1", 10);
        cacheService.zAdd("zSet_001", "hello, redis2", 20);
        cacheService.zAdd("zSet_001", "hello, redis3", 30);

        System.out.println(cacheService.zCount("zSet_001", 10, 40));
    }

    @Test
    public void cancelTask() {
        taskService.cancelTask(1747438965984333826L);
    }

    @Test
    public void pollTask() {
        Task task = taskService.poll(100, 50);
        System.out.println(task);
    }
}
