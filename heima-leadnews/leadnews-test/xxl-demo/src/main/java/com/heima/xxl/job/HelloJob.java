package com.heima.xxl.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class HelloJob {
    @XxlJob(value = "demoJobHandler")
    public void helloJob(){
        System.out.println("简单任务执行了。。。。");
    }


    /**
     * 2、分片广播任务
     */
    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        System.out.println("分片参数：当前分片序号 = {}, 总分片数 = {}" +  shardIndex + " "  + shardTotal);

        // 业务逻辑
        for (int i = 0; i < shardTotal; i++) {
            if (i == shardIndex) {
                System.out.println("第 {} 片, 命中分片开始处理" +  i);
            } else {
                System.out.println("第 {} 片, 忽略" +  i);
            }
        }

    }
}