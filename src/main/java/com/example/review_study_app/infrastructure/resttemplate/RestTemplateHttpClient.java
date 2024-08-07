package com.example.review_study_app.infrastructure.resttemplate;

import com.example.review_study_app.infrastructure.resttemplate.dto.MyHttpRequest;
import com.example.review_study_app.infrastructure.resttemplate.dto.MyHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RestTemplateHttpClient  {

    private final RestTemplate restTemplate;

    @Autowired
    public RestTemplateHttpClient(RestTemplate restTemplate) {
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

    public MyHttpResponse get(MyHttpRequest request) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            request.url(),
            HttpMethod.GET,
            new HttpEntity<>(request.body(), request.headers()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }

    public MyHttpResponse patch(MyHttpRequest request) throws Exception {
        ResponseEntity<String> response = restTemplate
            .exchange(
            request.url(),
            HttpMethod.PATCH,
            new HttpEntity<>(request.body(), request.headers()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}