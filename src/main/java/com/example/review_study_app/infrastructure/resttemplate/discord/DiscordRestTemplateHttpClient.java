package com.example.review_study_app.infrastructure.resttemplate.discord;

import com.example.review_study_app.infrastructure.resttemplate.common.dto.MyHttpRequest;
import com.example.review_study_app.infrastructure.resttemplate.common.dto.MyHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class DiscordRestTemplateHttpClient {

    private final RestTemplate restTemplate;

    @Autowired
    public DiscordRestTemplateHttpClient(
        @Qualifier("discordRestTemplate") RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }

    public MyHttpResponse post(MyHttpRequest request) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            request.url(),
            HttpMethod.POST,
            new HttpEntity<>(request.body(), request.headers()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}
