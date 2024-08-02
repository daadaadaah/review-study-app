package com.example.review_study_app.common.httpclient.dto;

import org.springframework.http.HttpHeaders;

public record MyHttpRequest<T>(
    String url,
    HttpHeaders headers,
    T body
) {

}
