package com.example.review_study_app.service.log;


import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.repository.log.LogRepository;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LogDirectSaveGoogleSheetsService implements LogService {

    private final LogRepository logRepository;

    private final LogHelper logHelper;

    private final LogSaveDiscordNotificationService logSaveDiscordNotificationService;

    @Autowired
    public LogDirectSaveGoogleSheetsService(
        LogRepository logRepository,
        LogHelper logHelper,
        LogSaveDiscordNotificationService logSaveDiscordNotificationService
    ) {
        this.logRepository = logRepository;
        this.logHelper = logHelper;
        this.logSaveDiscordNotificationService = logSaveDiscordNotificationService;
    }

    /**
     * saveJobLog는 Job 관련된 로그를 저장하는 메서드이다.
     *
     * < @Async("logSaveTaskExecutor") 추가한 이유 >
     * - 비즈니스 로직인 Github 작업과 로그 작업을 디커플링 시키기 위해
     * -
     *
     *
     */
    // TODO : 디스코드로! Job, Step, Task 모두 보내면, 디스코드 시끄러울 것 같은데, 이거 어떻게 할지 고민해보기
    @Async("logSaveHandlerExecutor") // TODO : 로그 저장 실패시, 비동기 예외 처리 어떻게 할 것인지 + 트랜잭션 처리
    public void saveJobLog(SaveJobLogDto saveJobLogDto) {

        BatchProcessType batchProcessType  = BatchProcessType.JOB;

        UUID jobId = saveJobLogDto.jobId();

        long jobDetailLogId = saveJobLogDto.endTime();

        JobDetailLog jobDetailLog = JobDetailLog.of(
            jobDetailLogId,
            logHelper.getEnvironment(),
            saveJobLogDto,
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        );

        ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
            jobId,
            null,
            logHelper.getEnvironment(),
            batchProcessType,
            saveJobLogDto.methodName(),
            saveJobLogDto.status(),
            saveJobLogDto.statusReason(),
            jobDetailLogId,
            saveJobLogDto.endTime() - saveJobLogDto.startTime(),
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        );

        try {

            logRepository.saveJobLogsWithTx(jobDetailLog, executionTimeLog);

            logSaveDiscordNotificationService.stackBatchProcessLogSaveSuccessResult(batchProcessType, jobId);

        } catch (Exception exception) {

            logSaveDiscordNotificationService.stackJobLogSaveFailureResult(exception, jobDetailLog, executionTimeLog);
        }

        logSaveDiscordNotificationService.sendBatchProcessResultsNotification();
    }

    @Async("logSaveHandlerExecutor")
    public void saveStepLog(SaveStepLogDto saveStepLogDto) {
        BatchProcessType batchProcessType  = BatchProcessType.STEP;

        UUID stepId = saveStepLogDto.stepId();

        long stepDetailLogId = saveStepLogDto.endTime();

        StepDetailLog stepDetailLog = StepDetailLog.of(
            stepDetailLogId,
            logHelper.getEnvironment(),
            saveStepLogDto,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        );

        long timeTaken = saveStepLogDto.endTime() - saveStepLogDto.startTime();

        ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
            saveStepLogDto.stepId(),
            saveStepLogDto.jobId(),
            logHelper.getEnvironment(),
            batchProcessType,
            saveStepLogDto.methodName(),
            saveStepLogDto.status(),
            saveStepLogDto.statusReason(),
            stepDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        );

        try {

            logRepository.saveStepLogsWithTx(stepDetailLog, executionTimeLog);

            logSaveDiscordNotificationService.stackBatchProcessLogSaveSuccessResult(batchProcessType, stepId);

        } catch (Exception exception) {

            logSaveDiscordNotificationService.stackStepLogSaveFailureResult(exception, stepDetailLog, executionTimeLog);
        }
    }

    @Async("logSaveHandlerExecutor")
    public void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {
        BatchProcessType batchProcessType  = BatchProcessType.TASK;

        UUID taskId = saveTaskLogDto.taskId();

        long taskDetailLogId = saveTaskLogDto.endTime();

        long timeTaken = saveTaskLogDto.endTime() - saveTaskLogDto.startTime();

        GithubApiLog githubApiLog = new GithubApiLog(
            taskDetailLogId,
            logHelper.getEnvironment(),
            saveTaskLogDto.batchProcessName(),
            saveTaskLogDto.httpMethod(),
            saveTaskLogDto.url(),
            saveTaskLogDto.requestHeaders(),
            saveTaskLogDto.requestBody(),
            saveTaskLogDto.responseStatusCode(),
            saveTaskLogDto.responseHeaders(),
            saveTaskLogDto.responseBody(),
            timeTaken,
            logHelper.getCreatedAt(saveTaskLogDto.endTime())
        );

        ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
            taskId,
            saveTaskLogDto.stepId(),
            logHelper.getEnvironment(),
            batchProcessType,
            saveTaskLogDto.batchProcessName(),
            saveTaskLogDto.status(),
            saveTaskLogDto.statusReason(),
            taskDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveTaskLogDto.endTime())
        );

        try {

            logRepository.saveGithubApiLogsWithTx(githubApiLog, executionTimeLog);

            logSaveDiscordNotificationService.stackBatchProcessLogSaveSuccessResult(batchProcessType, taskId);

        } catch (Exception exception) {

            logSaveDiscordNotificationService.stackTaskLogSaveFailureResult(exception, githubApiLog, executionTimeLog);
        }
    }
}
