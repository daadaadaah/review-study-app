package com.example.review_study_app.service.log;

import static com.example.review_study_app.infrastructure.resttemplate.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_FILE_COUNT;
import static com.example.review_study_app.infrastructure.resttemplate.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_MESSAGE_LENGTH;
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
import com.example.review_study_app.service.log.dto.LogSaveResult;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.enums.LogSaveResultType;
import com.example.review_study_app.service.log.helper.LogHelper;
import com.example.review_study_app.service.notification.NotificationService;
import com.example.review_study_app.service.notification.dto.UnSavedLogFile;
import com.example.review_study_app.service.notification.factory.file.UnSavedLogFileFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
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

    private final Stack<LogSaveResult> logSaveResultStack = new Stack<>();

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

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.SUCCESS,
                createJobLogsSaveSuccessMessage(jobId),
                new ArrayList<>()
            ));

        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedJobLogsFiles(jobDetailLog, executionTimeLog);

            String jobDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String jobExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveDetailLogFailureMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedJobLogsFiles(jobDetailLog, executionTimeLog);

            String jobDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String jobExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();


            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveRollbackSuccessMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedJobLogsFiles(jobDetailLog, executionTimeLog);

            String jobDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String jobExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveRollbackFailureMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (Exception exception) {

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedJobLogsFiles(jobDetailLog, executionTimeLog);

            String jobDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String jobExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createJobLogsSaveUnknownFailureMessage(
                    exception,
                    jobDetailLogFileName,
                    jobExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));
        }

        // TODO : 배치
        notifyBatchProcessTotalResult();
    }

    private void notifyBatchProcessTotalResult() {
        List<UnSavedLogFile> logFiles = new ArrayList<>();

        StringBuilder currentMessage = new StringBuilder("");

        while (!logSaveResultStack.isEmpty()) {

            LogSaveResult logSaveResult = logSaveResultStack.pop();

            String newMessage = logSaveResult.message();

            if(
                currentMessage.length() + newMessage.length() > MAX_DISCORD_MESSAGE_LENGTH ||
                logFiles.size() + logSaveResult.unSavedLogFiles().size() > MAX_DISCORD_FILE_COUNT
            ) {
                notificationService.sendMessageWithFiles(currentMessage.toString(), logFiles);

                currentMessage = new StringBuilder(newMessage);

                logFiles = new ArrayList<>(logSaveResult.unSavedLogFiles());

            } else {
                currentMessage.append("\n").append(newMessage);

                logFiles.addAll(logSaveResult.unSavedLogFiles());
            }
        }

        // 마지막으로 남은 메시지를 전송합니다.
        if (currentMessage.length() > 0) {
            notificationService.sendMessageWithFiles(currentMessage.toString(), logFiles);
        }
    }


    private List<UnSavedLogFile> createUnSavedJobLogsFiles(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog) {
        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String githubApiDetailLogFileName = unSavedLogFileFactory.createFileNameWithExtension(jobDetailLog.getClass().getSimpleName() +"_"+ jobDetailLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(githubApiDetailLogFileName, jobDetailLog));

        String githubApiExecutionTimeLogFileName = unSavedLogFileFactory.createFileNameWithExtension(executionTimeLog.getClass().getSimpleName() +"_"+ executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(githubApiExecutionTimeLogFileName, executionTimeLog));

        return unSavedLogFiles;
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

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.SUCCESS,
                createStepLogsSaveSuccessMessage(stepId),
                new ArrayList<>()
            ));

        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedStepLogsFiles(stepDetailLog, executionTimeLog);

            String stepDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String stepExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveDetailLogFailureMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedStepLogsFiles(stepDetailLog, executionTimeLog);

            String stepDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String stepExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveRollbackSuccessMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우


            List<UnSavedLogFile> unSavedLogFiles = createUnSavedStepLogsFiles(stepDetailLog, executionTimeLog);

            String stepDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String stepExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveRollbackFailureMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (Exception exception) {

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedStepLogsFiles(stepDetailLog, executionTimeLog);

            String stepDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String stepExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createStepLogsSaveUnKnownFailureMessage(
                    exception,
                    stepDetailLogFileName,
                    stepExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));
        }
    }


    private List<UnSavedLogFile> createUnSavedStepLogsFiles(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog) {
        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String githubApiDetailLogFileName = unSavedLogFileFactory.createFileNameWithExtension(stepDetailLog.getClass().getSimpleName() +"_"+ stepDetailLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(githubApiDetailLogFileName, stepDetailLog));

        String githubApiExecutionTimeLogFileName = unSavedLogFileFactory.createFileNameWithExtension(executionTimeLog.getClass().getSimpleName() +"_"+ executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(githubApiExecutionTimeLogFileName, executionTimeLog));

        return unSavedLogFiles;
    }


    @Async("logSaveTaskExecutor") // TODO : 이름이 배치 프로세스의 Task랑 헷갈린다. 다른 용어로 수정 필요!
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

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.SUCCESS,
                createGithubApiLogsSaveSuccessMessage(stepId),
                new ArrayList<>()
            ));

        } catch (SaveDetailLogException exception) { // 롤백 필요 없음
            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();;

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveDetailLogFailureMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우
            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveRollbackSuccessMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우
            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveRollbackFailureMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));
        } catch (Exception exception) {

            List<UnSavedLogFile> unSavedLogFiles = createUnSavedGithubApiLogFiles(githubApiLog, executionTimeLog);

            String githubApiDetailLogFileName = unSavedLogFiles.get(0).fileName();

            String githubApiExecutionTimeLogFileName = unSavedLogFiles.get(1).fileName();

            logSaveResultStack.add(new LogSaveResult(
                LogSaveResultType.FAILURE,
                createGithubApiLogsSaveUnKnownFailureMessage(
                    exception,
                    githubApiDetailLogFileName,
                    githubApiExecutionTimeLogFileName
                ),
                unSavedLogFiles
            ));
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
