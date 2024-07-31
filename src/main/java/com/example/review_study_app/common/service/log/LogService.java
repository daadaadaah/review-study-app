package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import com.example.review_study_app.common.service.log.entity.GithubApiLog;
import com.example.review_study_app.common.service.log.entity.JobDetailLog;
import com.example.review_study_app.common.service.log.entity.StepDetailLog;


public interface LogService {

    void saveJobLog(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog);

    void saveStepLog(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog);

    void saveTaskLog(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog);
}