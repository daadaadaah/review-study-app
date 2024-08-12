package com.example.review_study_app.infrastructure.resttemplate.discord.config;

import com.example.review_study_app.infrastructure.resttemplate.discord.DiscordApiLoggingInterceptor;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DiscordRestTemplateConfig {

    private final DiscordApiLoggingInterceptor discordApiLoggingInterceptor;

    @Autowired
    public DiscordRestTemplateConfig(DiscordApiLoggingInterceptor discordApiLoggingInterceptor) {
        this.discordApiLoggingInterceptor = discordApiLoggingInterceptor;
    }


    @Bean(name = "discordRestTemplate")
    public RestTemplate discordRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

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
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();

        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(httpRequestFactory));
        restTemplate.setInterceptors(Collections.singletonList(discordApiLoggingInterceptor));

        return restTemplate;
    }
}
