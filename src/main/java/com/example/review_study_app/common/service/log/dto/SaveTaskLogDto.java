package com.example.review_study_app.common.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import org.springframework.http.HttpHeaders;

public record SaveTaskLogDto<T>(
    BatchProcessStatus status,
    String statusReason,
    String httpMethod,
    String url,
    HttpHeaders requestHeaders,
    String requestBody,
    int responseStatusCode,
    HttpHeaders responseHeaders,
    String responseBody,
    long startTime,
    long endTime
) {

}
