package com.example.review_study_app.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * < 그냥 WebClient 이 아닌 WebClient.bulder()를 해준 이유 >
     * webclient를 사용하는 상황에 따라 커스텀하기 좋게 하기 위해서
     */
    @Bean
    public WebClient.Builder webclient() {
        return WebClient.builder();
    }
}
