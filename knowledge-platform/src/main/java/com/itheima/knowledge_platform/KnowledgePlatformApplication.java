package com.itheima.knowledge_platform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@EnableScheduling  //开启定时任务调度
@EnableTransactionManagement  //开启注解式事务管理
public class KnowledgePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgePlatformApplication.class, args);
        log.info("server started");
    }

}
