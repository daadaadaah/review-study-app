package com.example.review_study_app.task.httpclient.dto;

import org.springframework.http.HttpHeaders;

public record MyHttpResponse(
    int statusCode,
    HttpHeaders headers,
    String body
) {

}