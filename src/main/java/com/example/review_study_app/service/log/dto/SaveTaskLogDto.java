package com.example.review_study_app.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import java.util.UUID;
import org.springframework.http.HttpHeaders;

public record SaveTaskLogDto<T>(
    UUID taskId,
    UUID stepId,
    String batchProcessName,
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
