package com.example.review_study_app.common.httpclient;

import org.springframework.http.HttpHeaders;

public record MyHttpResponse(
    int statusCode,
    HttpHeaders headers,
    String body
) {

}