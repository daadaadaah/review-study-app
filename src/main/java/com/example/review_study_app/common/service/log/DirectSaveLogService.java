package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.repostiory.LogGoogleSheetsRepository;
import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import com.example.review_study_app.common.service.log.entity.GithubApiLog;
import com.example.review_study_app.common.service.log.entity.JobDetailLog;
import com.example.review_study_app.common.service.log.entity.StepDetailLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class DirectSaveLogService implements LogService {

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    @Autowired
    public DirectSaveLogService(
        LogGoogleSheetsRepository logGoogleSheetsRepository
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
    }

    @Async("logSaveTaskExecutor")
    public void saveJobLog(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog) {
        logGoogleSheetsRepository.save(jobDetailLog);

        logGoogleSheetsRepository.save(executionTimeLog);
    }

    @Async("logSaveTaskExecutor")
    public void saveStepLog(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog) {
        logGoogleSheetsRepository.save(stepDetailLog);

        logGoogleSheetsRepository.save(executionTimeLog);
    }

    @Async("logSaveTaskExecutor")
    public void saveTaskLog(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {
        logGoogleSheetsRepository.save(githubApiLog);

        logGoogleSheetsRepository.save(executionTimeLog);
    }
}
