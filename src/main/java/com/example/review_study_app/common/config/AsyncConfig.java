package com.example.review_study_app.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "logSaveHandlerExecutor")
    public TaskExecutor logSaveHandlerExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1); // 순서 보장하기 위해서 1개
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("LogSaveHandler-"); // 스레드 이름 접두사
        executor.initialize();
        return executor;
    }
}
