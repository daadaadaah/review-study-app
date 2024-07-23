package com.example.review_study_app.common.config;

import com.example.review_study_app.common.httpclient.RestTemplateLoggingGitHubApiInterceptor;
import com.example.review_study_app.log.LogGoogleSheetsRepository;
import com.example.review_study_app.log.LogHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    private final LogHelper logHelper;

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    @Autowired
    public AppConfig(
        LogHelper logHelper,
        LogGoogleSheetsRepository logGoogleSheetsRepository
    ) {
        this.logHelper = logHelper;
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
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
     * 2. restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory())) 추가한 이유
     * - 일반적으로 HTTP 요청 및 응답의 본문은 InputStream 또는 OutputStream을 통해 전달된다.
     * - 따라서, ClientHttpRequestInterceptor을 구현한 RestTemplateLoggingGitHubApiInterceptor에서 로깅을 위해 응답 객체를 1번 소비하면, 사라지게 되어, 실제 비즈니스 로직에서 응답 처리가 정상적으로 되지 않는 문제가 발생한다.
     * - 따라서, 이를 해결하기 위해, BufferingClientHttpRequestFactory를 사용한다.
     * - BufferingClientHttpRequestFactory는 스트림을 버퍼에 저장하여(=스트림 데이터의 복사본을 메모리에 저장하므로) 여러 번 읽을 수 있도록 함으로써, 로깅과 같은 기능이 스트림을 소비하더라도, 실제 비즈니스 로직에서의 요청 및 응답 처리에 영향을 주지 않게 된다.
     * > 참고 : https://hoonmaro.tistory.com/49
     *
     *
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.setInterceptors(Collections.singletonList(new RestTemplateLoggingGitHubApiInterceptor(logGoogleSheetsRepository, logHelper))); // TODO : RestTemplateLoggingGitHubApiInterceptor 를 Bean 등록 고려해보기
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}