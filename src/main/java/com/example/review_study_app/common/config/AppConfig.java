package com.example.review_study_app.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * @scheduled를 어떤 스레드 환경에서 동작하게 할 것인지 설정하는 메서드이다.
     * Spring의 @Scheduled는 기본적으로 TaskScheduler 인터페이스의 구현체인 ConcurrentTaskScheduler를 사용한다.
     * 따라서, 별도의 설정 없이 사용하면 SingleThreadScheduledExecutor를 사용하게 되어, 한 번에 하나의 스케줄링 작업만 수행한다.
     * 만약, setPoolSize(원하는 스레드 수)을 통해 원하는 스레드 수를 설정하면 된다.
     */
    @Bean
    public TaskScheduler scheduledTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ScheduledTask-"); // 스레드 이름 접두사
        scheduler.initialize();
        return scheduler;
    }
}
