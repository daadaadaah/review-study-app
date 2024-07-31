package com.example.review_study_app.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "eventTaskExecutor")
    public TaskExecutor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int n = Runtime.getRuntime().availableProcessors(); // core 갯수

        executor.setCorePoolSize(1); // 순서 보장하기 위해서 1개
//        executor.setMaxPoolSize(10);
//        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("EventTask-"); // 스레드 이름 접두사
        executor.initialize();
        return executor;
    }
}


