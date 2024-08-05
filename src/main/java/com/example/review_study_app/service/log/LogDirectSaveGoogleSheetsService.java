package com.example.review_study_app.service.log;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.infrastructure.googlesheets.exception.CreateSheetsFailException;
import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.common.httpclient.dto.MyHttpResponse;
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

        String newRange = null;

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

            newRange = logGoogleSheetsRepository.saveJobDetailLog(jobDetailLog);

            logGoogleSheetsRepository.saveExecutionTimeLog(executionTimeLog);

            String message = String.format("Job 로그 저장 성공 : jobId=%s", jobId);

            notificationService.sendMessage(message);

        } catch (CreateSheetsFailException createSheetsFailException) {

            String message = String.format(""
                    + DiscordNotificationService.EMOJI_WARING+"Job 로그 저장 실패 (원인 : 구글 시트 객체 생성 실패 - %s)"+DiscordNotificationService.EMOJI_WARING+"\n"
                    + "<예외 메시지> \n"
                    + "- %s \n"
                    + "<저장되지 않는 로그> \n"
                    + "- jobDetailLog=%s \n"
                    + "- executionTimeLog=%s",
                createSheetsFailException.getReason(),
                createSheetsFailException.getMessage(),
                jobDetailLog,
                executionTimeLog
            );

            notificationService.sendMessage(message);

        } catch (IOException ioException) {
            if(newRange == null) { // saveJobDetailLog 저장하면서 IOException 발생한 상황
                // 롤백 안해도 됨
                notificationService.sendMessage("로그 저장 실패 (원인 : "+ioException.getMessage()+")");


            } else { // saveJobDetailLog은 성공했으나, saveExecutionTimeLog 저장하면서 IOException 발생한 상황
                // TODO : 롤백 처리 해야 함.
                try {
                    logGoogleSheetsRepository.remove(newRange);
                } catch (Exception exception) {

                }
                notificationService.sendMessage("로그 저장 실패 (원인 : "+ioException.getMessage()+")");
            }
        } catch (Exception exception) {

            notificationService.sendMessage("Job 로그 저장 실패(원인 : 예상치 못한 예외 발생) jobId="+jobId+", ex="+exception.getMessage());
        }
    }

    @Async("logSaveTaskExecutor")
    public void saveStepLog(SaveStepLogDto saveStepLogDto) {

        UUID jobId = saveStepLogDto.jobId();


        try {

            long stepDetailLogId = saveStepLogDto.endTime();

            logGoogleSheetsRepository.saveStepDetailLog(StepDetailLog.of(
                stepDetailLogId,
                logHelper.getEnvironment(),
                saveStepLogDto,
                logHelper.getCreatedAt(saveStepLogDto.endTime())
            ));

            long timeTaken = saveStepLogDto.endTime() - saveStepLogDto.startTime();

            logGoogleSheetsRepository.saveExecutionTimeLog(ExecutionTimeLog.of(
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
            ));

        } catch (IOException exception) {
            // 디테일로그 저장 성공. 그러나, 실행 로그 저장 실패
            // TODO : 롤백
            notificationService.sendMessage("Job 로그 저장 실패(원인 : IOException) jobId="+jobId+", ex="+exception.getMessage());

        } catch (Exception exception) {

            notificationService.sendMessage("Job 로그 저장 실패(원인 : 예상치 못한 예외 발생) jobId="+jobId+", ex="+exception.getMessage());

            // jobId, 실패 이유
            // stepId, parentId 실패 이유
            // taskId, parentId 실패 이유

//            notificationService.createIssueCloseFailureMessage();
        }
    }

    @Async("logSaveTaskExecutor")
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
