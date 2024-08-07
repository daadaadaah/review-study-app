package com.example.review_study_app.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.infrastructure.resttemplate.dto.MyHttpRequest;
import java.util.UUID;

public record SaveTaskLogDto<T>(
    UUID taskId,
    UUID stepId,
    String batchProcessName,
    BatchProcessStatus status,
    String statusReason,
    String httpMethod,
    MyHttpRequest myHttpRequest,

    T taskResult,
    long startTime,
    long endTime
) {

}
