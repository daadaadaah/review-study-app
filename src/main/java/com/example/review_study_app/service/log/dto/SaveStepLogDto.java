package com.example.review_study_app.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import java.util.UUID;

public record SaveStepLogDto<T>(
    UUID stepId,
    UUID jobId,
    String methodName,
    BatchProcessStatus status,
    String statusReason,
    T result,
    long startTime,
    long endTime
) {

}
