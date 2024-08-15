package com.example.review_study_app.service.log;

import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.*;
import static com.example.review_study_app.service.notification.factory.message.JobLogsSaveMessageFactory.*;
import static com.example.review_study_app.service.notification.factory.message.StepLogsSaveMessageFactory.*;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.repository.log.exception.GoogleSheetsRollbackFailureException;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import com.example.review_study_app.service.notification.NotificationService;
import com.example.review_study_app.service.notification.dto.UnSavedLogFile;
import com.example.review_study_app.service.notification.factory.file.UnSavedLogFileFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LogDirectSaveGoogleSheetsService implements LogService {

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    private final NotificationService notificationService;

    private final LogHelper logHelper;

    private final UnSavedLogFileFactory unSavedLogFileFactory;

    @Autowired
    public LogDirectSaveGoogleSheetsService(
        LogGoogleSheetsRepository logGoogleSheetsRepository,
        NotificationService notificationService,
        LogHelper logHelper,
        UnSavedLogFileFactory unSavedLogFileFactory
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
        this.notificationService = notificationService;
        this.logHelper = logHelper;
        this.unSavedLogFileFactory = unSavedLogFileFactory;
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


    @Async("logSaveTaskExecutor")
    public void saveStepLog(SaveStepLogDto saveStepLogDto) {
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
            logGoogleSheetsRepository.saveStepLogsWithTx(stepDetailLog, executionTimeLog);

            notificationService.sendMessage(createStepLogsSaveSuccessMessage(stepId));
        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

            notificationService.sendMessage(createStepLogsSaveDetailLogFailureMessage(
                exception,
                stepDetailLog,
                executionTimeLog
            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우

            notificationService.sendMessage(createStepLogsSaveRollbackSuccessMessage(
                exception,
                stepDetailLog,
                executionTimeLog,
                exception.getGoogleSheetsRollbackRange()
            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우

            notificationService.sendMessage(createStepLogsSaveRollbackFailureMessage(
                exception,
                stepDetailLog,
                executionTimeLog,
                exception.getGoogleSheetsRollbackRange()
            ));

        } catch (Exception exception) {

            notificationService.sendMessage(createStepLogsSaveUnKnownFailureMessage(
                exception,
                stepDetailLog,
                executionTimeLog
            ));
        }
    }

    @Async("logSaveTaskExecutor") // TODO : resttemplate 통신 로그 저장 방법 AOP -> 인터셉터로 바꾼 후에 수정하기
    public void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {

        UUID stepId = saveTaskLogDto.stepId();

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
        );

        try {

            logGoogleSheetsRepository.saveGithubApiLogsWithTx(githubApiLog, executionTimeLog);

            notificationService.sendMessage(createGithubApiLogsSaveSuccessMessage(stepId));

        } catch (SaveDetailLogException exception) { // 롤백 필요 없음
            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            notificationService.sendMessageWithFiles(createGithubApiLogsSaveDetailLogFailureMessage(
                exception,
                githubApiDetailLogFileName,
                githubApiExecutionTimeLogFileName
            ), unSavedLogFiles);

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우
            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            notificationService.sendMessageWithFiles(createGithubApiLogsSaveRollbackSuccessMessage(
                exception,
                githubApiDetailLogFileName,
                githubApiExecutionTimeLogFileName
            ), unSavedLogFiles);

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우
            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            notificationService.sendMessageWithFiles(createGithubApiLogsSaveRollbackFailureMessage(
                exception,
                githubApiDetailLogFileName,
                githubApiExecutionTimeLogFileName
            ), unSavedLogFiles);

        } catch (Exception exception) {

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            notificationService.sendMessageWithFiles(
                createGithubApiLogsSaveUnKnownFailureMessage(exception, githubApiDetailLogFileName, githubApiExecutionTimeLogFileName),
                unSavedLogFiles
            );
        }
    }

    private List<UnSavedLogFile> createUnSavedGithubApiLogFiles(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {
        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String githubApiDetailLogFileName = unSavedLogFileFactory.createFileNameWithExtension(githubApiLog.getClass().getSimpleName() +"_"+ githubApiLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(githubApiDetailLogFileName, githubApiLog));

        String githubApiExecutionTimeLogFileName = unSavedLogFileFactory.createFileNameWithExtension(executionTimeLog.getClass().getSimpleName() +"_"+ executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(githubApiExecutionTimeLogFileName, executionTimeLog));

        return unSavedLogFiles;
    }
}
