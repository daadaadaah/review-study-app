package com.example.review_study_app.common.dto;

import org.springframework.http.HttpHeaders;

public record MyHttpResponse(
    int statusCode,
    HttpHeaders headers,
    String body
) {

}