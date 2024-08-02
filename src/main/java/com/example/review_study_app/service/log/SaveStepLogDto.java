package com.example.review_study_app.service.log;

import com.example.review_study_app.common.enums.BatchProcessStatus;

public record SaveStepLogDto<T>(
    String methodName,
    BatchProcessStatus status,
    String statusReason,
    T result,
    long startTime,
    long endTime
) {

}
