package com.example.review_study_app.service.log;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.common.httpclient.dto.MyHttpResponse;
import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.repository.log.exception.GoogleSheetsRollbackFailureException;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.repository.log.exception.SaveExecutionTimeLogException;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import com.example.review_study_app.service.notification.DiscordNotificationService;
import com.example.review_study_app.service.notification.NotificationService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;


@Slf4j
@Service
public class LogDirectSaveGoogleSheetsService implements LogService {

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    private final NotificationService notificationService;

    private final LogHelper logHelper;

    private final List<String> exceptions = new ArrayList<>();

    @Autowired
    public LogDirectSaveGoogleSheetsService(
        LogGoogleSheetsRepository logGoogleSheetsRepository,
        NotificationService notificationService,
        LogHelper logHelper
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
        this.notificationService = notificationService;
        this.logHelper = logHelper;
    }

    private void rollback(String rangeToRemoved) throws IOException {
        logGoogleSheetsRepository.remove(rangeToRemoved);
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
    @Async("logSaveTaskExecutor") // TODO : 로그 저장 실패시, 비동기 예외 처리 어떻게 할 것인지 + 트랜잭션 처리
    public void saveJobLog(SaveJobLogDto saveJobLogDto) {

        String newJobDetailLogRange = null;

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
            BatchProcessType.JOB,
            saveJobLogDto.methodName(),
            saveJobLogDto.status(),
            saveJobLogDto.statusReason(),
            jobDetailLogId,
            saveJobLogDto.endTime() - saveJobLogDto.startTime(),
            logHelper.getCreatedAt(saveJobLogDto.startTime())
        );

        try {
            logGoogleSheetsRepository.saveJobLogsWithTx(jobDetailLog, executionTimeLog);

            notificationService.sendMessage(createJobLogsSaveSuccessMessage(jobId));
        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

            notificationService.sendMessage(createJobLogsSaveDetailLogFailureMessage(
                exception,
                jobDetailLog,
                executionTimeLog
            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우
            notificationService.sendMessage(createJobLogsSaveRollbackSuccessMessage(
                exception,
                jobDetailLog,
                executionTimeLog,
                newJobDetailLogRange
            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우

            notificationService.sendMessage(createJobLogsSaveRollbackFailureMessage(
                exception,
                jobDetailLog,
                executionTimeLog,
                newJobDetailLogRange
            ));

        } catch (Exception exception) {
            notificationService.sendMessage(createJobLogsSaveUnknownFailureMessage(
                exception,
                jobDetailLog,
                executionTimeLog
            ));
        }
    }

    private String createJobLogsSaveSuccessMessage(UUID jobId) {
        return String.format(
            "%sJob 로그 저장 성공 : jobId=%s %s",
            DiscordNotificationService.EMOJI_CONGRATS,
            jobId,
            DiscordNotificationService.EMOJI_CONGRATS
        );
    }

    private String createJobLogsSaveDetailLogFailureMessage(
        SaveDetailLogException exception,
        JobDetailLog jobDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {
        return String.format(
            "%sJob 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- jobDetailLog=%s \n"
                + "- executionTimeLog=%s",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            jobDetailLog,
            executionTimeLog
        );
    }

    private String createJobLogsSaveRollbackSuccessMessage(
        GoogleSheetsTransactionException exception,
        JobDetailLog jobDetailLog,
        ExecutionTimeLog executionTimeLog,
        String range
    ) {
        return String.format(
            "%sJob 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- jobDetailLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- true (range : %s)",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            jobDetailLog,
            executionTimeLog,
            range
        );
    }

    private String createJobLogsSaveRollbackFailureMessage(
        Exception rollbackException,
        JobDetailLog jobDetailLog,
        ExecutionTimeLog executionTimeLog,
        String range
    ) {
        return String.format(
            "%sJob 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- jobDetailLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- false (range : %s) \n"
                + "- 예외 메시지 : %s",
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getMessage(),
            jobDetailLog,
            executionTimeLog,
            range == null ? "" : range,
            rollbackException.getMessage()
        );
    }

    private String createJobLogsSaveUnknownFailureMessage(
        Exception exception,
        JobDetailLog jobDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {
        return String.format(
            "%sJob 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- jobDetailLog=%s \n"
                + "- executionTimeLog=%s",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            jobDetailLog,
            executionTimeLog
        );
    }

    @Async("logSaveTaskExecutor")
    public void saveStepLog(SaveStepLogDto saveStepLogDto) {

        String newStepDetailLogRange = null;

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
            BatchProcessType.STEP,
            saveStepLogDto.methodName(),
            saveStepLogDto.status(),
            saveStepLogDto.statusReason(),
            stepDetailLogId,
            timeTaken,
            logHelper.getCreatedAt(saveStepLogDto.endTime())
        );

        try {

            newStepDetailLogRange = logGoogleSheetsRepository.saveStepDetailLog(stepDetailLog);

            if(newStepDetailLogRange != null) {
                logGoogleSheetsRepository.saveExecutionTimeLog(executionTimeLog);
            }

            notificationService.sendMessage(createStepLogsSaveSuccessMessage(stepId));

        } catch (SaveExecutionTimeLogException exception) {

            try {
                rollback(newStepDetailLogRange); // 위에서 newRange이 null 이 아닌 경우, 저장하므로, 이 예외가 발생한 경우 무조건 rollback 필요!

                notificationService.sendMessage(createStepLogsSaveRollbackSuccessMessage(
                    exception,
                    stepDetailLog,
                    executionTimeLog,
                    newStepDetailLogRange
                ));

            } catch (Exception rollbackException) {

                notificationService.sendMessage(createStepLogsSaveRollbackFailureMessage(
                    rollbackException,
                    stepDetailLog,
                    executionTimeLog,
                    newStepDetailLogRange
                ));
            }

        } catch (Exception exception) {

            notificationService.sendMessage(createStepLogsSaveUnKnownFailureMessage(
                exception,
                stepDetailLog,
                executionTimeLog
            ));
        }
    }

    private String createStepLogsSaveSuccessMessage(UUID stepId) {
        return String.format(
            "%sStep 로그 저장 성공 : stepId=%s %s",
            DiscordNotificationService.EMOJI_CONGRATS,
            stepId,
            DiscordNotificationService.EMOJI_CONGRATS
        );
    }

    private String createStepLogsSaveRollbackSuccessMessage(
        Exception exception,
        StepDetailLog stepDetailLog,
        ExecutionTimeLog executionTimeLog,
        String range
    ){
        return String.format(
            "%sStep 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- stepDetailLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- %s (range : %s)",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            stepDetailLog,
            executionTimeLog,
            "true",
            range
        );
    }

    private String createStepLogsSaveRollbackFailureMessage(
        Exception rollbackException,
        StepDetailLog stepDetailLog,
        ExecutionTimeLog executionTimeLog,
        String range
    ) {
        return String.format(
            "%sStep 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- stepDetailLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- false (range : %s) \n"
                + "- 예외 메시지 : %s",
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getMessage(),
            stepDetailLog,
            executionTimeLog,
            range == null ? "" : range,
            rollbackException.getMessage()
        );
    }

    private String createStepLogsSaveUnKnownFailureMessage(
        Exception exception,
        StepDetailLog stepDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {
        return String.format(
            "%sStep 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- stepDetailLog=%s \n"
                + "- executionTimeLog=%s \n",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            stepDetailLog,
            executionTimeLog
        );
    }


    @Async("logSaveTaskExecutor") // TODO : resttemplate 통신 로그 저장 방법 AOP -> 인터셉터로 바꾼 후에 수정하기
    public void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {

        UUID stepId = saveTaskLogDto.stepId();


        try {
            long taskDetailLogId = saveTaskLogDto.endTime();

            long timeTaken = saveTaskLogDto.endTime() - saveTaskLogDto.startTime();

            if(saveTaskLogDto.taskResult() instanceof MyHttpResponse) {

                MyHttpResponse myHttpResponse = (MyHttpResponse) saveTaskLogDto.taskResult();

                String requestBody = saveTaskLogDto.myHttpRequest().body() != null ? saveTaskLogDto.myHttpRequest().body().toString() : null;

                logGoogleSheetsRepository.saveGithubApiLog(new GithubApiLog(
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

                logGoogleSheetsRepository.saveGithubApiLog(new GithubApiLog(
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

            logGoogleSheetsRepository.saveExecutionTimeLog(ExecutionTimeLog.of(
                saveTaskLogDto.taskId(),
                saveTaskLogDto.stepId(),
                logHelper.getEnvironment(),
                BatchProcessType.TASK,
                saveTaskLogDto.batchProcessName(),
                saveTaskLogDto.status(),
                saveTaskLogDto.statusReason(),
                taskDetailLogId,
                timeTaken,
                logHelper.getCreatedAt(saveTaskLogDto.endTime())
            ));
        } catch (IOException exception) {
            // 디테일로그 저장 성공. 그러나, 실행 로그 저장 실패
            // TODO : 롤백

            notificationService.sendMessage("Job 로그 저장 실패(원인 : 예상치 못한 예외 발생) stepId="+stepId+", ex="+exception.getMessage());

        } catch (Exception exception) {


            notificationService.sendMessage("Job 로그 저장 실패(원인 : 예상치 못한 예외 발생) stepId="+stepId+", ex="+exception.getMessage());

            // jobId, 실패 이유
            // stepId, parentId 실패 이유
            // taskId, parentId 실패 이유

//            notificationService.createIssueCloseFailureMessage();
        }
    }
}
