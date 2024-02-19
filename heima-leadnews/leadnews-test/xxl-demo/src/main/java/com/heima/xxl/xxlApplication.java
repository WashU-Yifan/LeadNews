package com.heima.xxl;

import com.heima.xxl.job.HelloJob;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class xxlApplication {
    public static void main(String[] args) {

        SpringApplication.run(xxlApplication.class, args);
        System.out.println("启动");
    }
}
