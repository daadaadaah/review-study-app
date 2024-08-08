package com.example.review_study_app.infrastructure.resttemplate.common.dto;

import org.springframework.http.HttpHeaders;

public record MyHttpRequest<T>(
    String url,
    HttpHeaders headers,
    T body
) {

}
