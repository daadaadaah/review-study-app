package com.example.review_study_app.task.httpclient.dto;

import org.springframework.http.HttpHeaders;

public record MyHttpRequest<T>(
    String url,
    HttpHeaders headers,
    T body
) {

}
