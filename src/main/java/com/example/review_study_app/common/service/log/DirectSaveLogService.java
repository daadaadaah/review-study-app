package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.common.repostiory.LogGoogleSheetsRepository;
import com.example.review_study_app.common.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.common.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.common.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import com.example.review_study_app.common.service.log.entity.GithubApiLog;
import com.example.review_study_app.common.service.log.entity.JobDetailLog;
import com.example.review_study_app.common.service.log.entity.StepDetailLog;
import com.example.review_study_app.task.httpclient.dto.MyHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;


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
    public void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {

        long taskDetailLogId = saveTaskLogDto.endTime();

        long timeTaken = saveTaskLogDto.endTime() - saveTaskLogDto.startTime();

        if(saveTaskLogDto.taskResult() instanceof MyHttpResponse) {

            MyHttpResponse myHttpResponse = (MyHttpResponse) saveTaskLogDto.taskResult();

            String requestBody = saveTaskLogDto.myHttpRequest().body() != null ? saveTaskLogDto.myHttpRequest().body().toString() : null;

            logGoogleSheetsRepository.save(new GithubApiLog(
                taskDetailLogId,
                logHelper.getEnvironment(),
                saveTaskLogDto.batchProcessName(),
                saveTaskLogDto.httpMethod(),
                saveTaskLogDto.myHttpRequest().url(),
                saveTaskLogDto.myHttpRequest().headers(),
                requestBody,
                myHttpResponse.statusCode(),
                myHttpResponse.headers(),
                myHttpResponse.body(),
                timeTaken,
                logHelper.getCreatedAt(saveTaskLogDto.endTime())
            ));
        } else if(saveTaskLogDto.taskResult() instanceof RestClientResponseException) {
            RestClientResponseException restClientResponseException = (RestClientResponseException) saveTaskLogDto.taskResult();

            String requestBody = saveTaskLogDto.myHttpRequest().body() != null ? saveTaskLogDto.myHttpRequest().body().toString() : null;

            logGoogleSheetsRepository.save(new GithubApiLog(
                taskDetailLogId,
                logHelper.getEnvironment(),
                saveTaskLogDto.batchProcessName(),
                saveTaskLogDto.httpMethod(),
                saveTaskLogDto.myHttpRequest().url(),
                saveTaskLogDto.myHttpRequest().headers(),
                requestBody,
                restClientResponseException.getStatusCode().value(),
                restClientResponseException.getResponseHeaders(),
                restClientResponseException.getResponseBodyAsString(),
                timeTaken,
                logHelper.getCreatedAt(saveTaskLogDto.endTime())
            ));
        } else {
            // TODO :
        }

        logGoogleSheetsRepository.save(ExecutionTimeLog.of(
            logHelper.getTaskId(),
            logHelper.getStepId(),
            logHelper.getEnvironment(),
            BatchProcessType.TASK,
            saveTaskLogDto.batchProcessName(),
            saveTaskLogDto.status(),
            saveTaskLogDto.statusReason(),
            taskDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveTaskLogDto.endTime())
        ));
    }
}
