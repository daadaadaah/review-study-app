package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.common.repostiory.LogGoogleSheetsRepository;
import com.example.review_study_app.common.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.common.service.log.dto.SaveStepLogDto;
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

    private final LogHelper logHelper;

    @Autowired
    public DirectSaveLogService(
        LogGoogleSheetsRepository logGoogleSheetsRepository,
        LogHelper logHelper
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
        this.logHelper = logHelper;
    }

    @Async("logSaveTaskExecutor")
    public void saveJobLog(SaveJobLogDto saveJobLogDto) {

        long jobDetailLogId = saveJobLogDto.endTime();

        logGoogleSheetsRepository.save(JobDetailLog.of(
            jobDetailLogId,
            logHelper.getEnvironment(),
            saveJobLogDto,
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        ));

        logGoogleSheetsRepository.save(ExecutionTimeLog.of(
            logHelper.getJobId(),
            null,
            logHelper.getEnvironment(),
            BatchProcessType.JOB,
            saveJobLogDto.methodName(),
            saveJobLogDto.status(),
            saveJobLogDto.statusReason(),
            jobDetailLogId,
            saveJobLogDto.endTime() - saveJobLogDto.startTime(),
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        ));
    }

    @Async("logSaveTaskExecutor")
    public void saveStepLog(SaveStepLogDto saveStepLogDto) {

        long stepDetailLogId = saveStepLogDto.endTime();

        logGoogleSheetsRepository.save(StepDetailLog.of(
            stepDetailLogId,
            logHelper.getEnvironment(),
            saveStepLogDto,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        ));

        long timeTaken = saveStepLogDto.endTime() - saveStepLogDto.startTime();

        logGoogleSheetsRepository.save(ExecutionTimeLog.of(
            logHelper.getStepId(),
            logHelper.getJobId(),
            logHelper.getEnvironment(),
            BatchProcessType.STEP,
            saveStepLogDto.methodName(),
            saveStepLogDto.status(),
            saveStepLogDto.statusReason(),
            stepDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        ));
    }

    @Async("logSaveTaskExecutor")
    public void saveTaskLog(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {
        logGoogleSheetsRepository.save(githubApiLog);

        logGoogleSheetsRepository.save(executionTimeLog);
    }
}
