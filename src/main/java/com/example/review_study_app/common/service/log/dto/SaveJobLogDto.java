package com.example.review_study_app.common.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.job.dto.JobResult;

public record SaveJobLogDto(
    String methodName,
    BatchProcessStatus status,
    String statusReason,
    JobResult jobResult,
    long startTime,
    long endTime
) {

}
