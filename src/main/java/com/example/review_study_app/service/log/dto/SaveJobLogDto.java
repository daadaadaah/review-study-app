package com.example.review_study_app.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.service.github.dto.GithubJobResult;
import java.util.UUID;

public record SaveJobLogDto(
    UUID jobId,
    String methodName,
    BatchProcessStatus status,
    String statusReason,
    GithubJobResult githubJobResult,
    long startTime,
    long endTime
) {

}
