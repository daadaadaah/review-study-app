package com.example.review_study_app.common.httpclient;

import com.example.review_study_app.notification.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// 디스코드 통신 때 아직 사용하고 있어서 아직 삭제하면 안됨

@Slf4j
@Service
public class RestTemplateHttpClient implements MyHttpClient  {

    private final RestTemplate restTemplate;

    @Autowired
    public RestTemplateHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MyHttpResponse post(MyHttpRequest request) throws Exception {

        HttpEntity<Object> messageEntity = new HttpEntity<>(request.body(), request.headers());

        ResponseEntity<String> response = restTemplate.exchange(
            request.url(),
            HttpMethod.POST,
            messageEntity,
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}