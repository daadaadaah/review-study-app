package com.example.review_study_app.repository.log;

import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;


public interface LogRepository {

    void saveJobLogsWithTx(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog);

    void saveStepLogsWithTx(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog);

    void saveGithubApiLogsWithTx(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog);
}

