package com.example.review_study_app.common.service.log.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.service.github.dto.GithubJobResult;

public record SaveJobLogDto(
    String methodName,
    BatchProcessStatus status,
    String statusReason,
    GithubJobResult githubJobResult,
    long startTime,
    long endTime
) {

}
