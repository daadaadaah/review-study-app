package com.example.review_study_app.service.log;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.task.httpclient.dto.MyHttpRequest;

public record SaveTaskLogDto<T>(
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
