package com.example.review_study_app.common.config;

import com.example.review_study_app.infrastructure.resttemplate.interceptor.GitHubApiLoggingInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    private final GitHubApiLoggingInterceptor gitHubApiLoggingInterceptor;

    @Autowired
    public AppConfig(
        GitHubApiLoggingInterceptor gitHubApiLoggingInterceptor
    ) {
        this.gitHubApiLoggingInterceptor = gitHubApiLoggingInterceptor;
    }

    /**
     *
     * 1. restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); 추가한 이유
     * - 스프링에서 Spring에서 RestTemplate을 사용하여 PATCH 메서드를 실행하려면 HttpComponentsClientHttpRequestFactory를 설정해야 한다.
     * - 기본적으로 Spring의 RestTemplate은 SimpleClientHttpRequestFactory를 사용하며, 이 기본 팩토리는 PATCH 메서드를 지원하지 않는다.
     * - 따라서, PATCH 요청을 사용하려면, 다음과 같이 의존성을 추가하고, HTTP 클라이언트 라이브러리를 설정해줘야 한다.
     *      1) build.gradle에 의존성 추가
     *      - implementation 'org.apache.httpcomponents.client5:httpclient5:5.3.1'
     *      2) RestTemplate에 HttpComponentsClientHttpRequestFactory 설정 추가
     *      - restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
     * - 위와 같이 해주지 않으면, 다음과 같은 에러가 발생한다.
     * 에러 : I/O error on PATCH request for "http://~~": Invalid HTTP method: PATCH
     *
     *
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.setInterceptors(Collections.singletonList(gitHubApiLoggingInterceptor));
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        return restTemplate;
    }

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
