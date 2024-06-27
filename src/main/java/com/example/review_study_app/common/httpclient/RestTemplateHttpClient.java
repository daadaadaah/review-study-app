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
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; utf-8");

        HttpEntity<NotificationMessage> messageEntity = new HttpEntity<>(new NotificationMessage(request.message()), httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            request.url(),
            HttpMethod.POST,
            messageEntity,
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}