package com.example.review_study_app.service.log;

import static com.example.review_study_app.infrastructure.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_FILE_COUNT;
import static com.example.review_study_app.infrastructure.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_MESSAGE_LENGTH;
import static com.example.review_study_app.service.notification.factory.message.BatchProcessLogsSaveMessageFactory.createLogsSaveFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.BatchProcessLogsSaveMessageFactory.createLogsSaveSuccessMessage;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.service.log.vo.LogSaveResult;
import com.example.review_study_app.service.notification.NotificationService;
import com.example.review_study_app.service.notification.vo.UnSavedLogFile;
import com.example.review_study_app.service.notification.factory.file.UnSavedLogFileFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LogSaveDiscordNotificationService {

    private NotificationService notificationService;

    private final UnSavedLogFileFactory unSavedLogFileFactory;

    private final Stack<LogSaveResult> logSaveResultStack = new Stack<>();

    @Autowired
    public LogSaveDiscordNotificationService(
        NotificationService notificationService,
        UnSavedLogFileFactory unSavedLogFileFactory
    ) {
        this.notificationService = notificationService;
        this.unSavedLogFileFactory = unSavedLogFileFactory;
    }

    public void sendBatchProcessResultsNotification() {
        List<UnSavedLogFile> logFiles = new ArrayList<>();

        StringBuilder currentMessage = new StringBuilder("");

        while (!logSaveResultStack.isEmpty()) {
            LogSaveResult logSaveResult = logSaveResultStack.pop();

            String newMessage = logSaveResult.message();

            if (
                currentMessage.length() + newMessage.length() > MAX_DISCORD_MESSAGE_LENGTH
                || logFiles.size() + logSaveResult.unSavedLogFiles().size() > MAX_DISCORD_FILE_COUNT
            ) {

                notificationService.sendMessageWithFiles(currentMessage.toString(), logFiles);

                currentMessage = new StringBuilder(newMessage);

                logFiles = new ArrayList<>(logSaveResult.unSavedLogFiles());

            } else {
                currentMessage.append("\n").append(newMessage);

                logFiles.addAll(logSaveResult.unSavedLogFiles());
            }
        }

        if (currentMessage.length() > 0) {
            notificationService.sendMessageWithFiles(currentMessage.toString(), logFiles);
        }
    }

    public void stackBatchProcessLogSaveSuccessResult(
        BatchProcessType batchProcessType,
        UUID batchProcessId
    ) {
        stackLogSaveResult(
            createLogsSaveSuccessMessage(batchProcessType, batchProcessId),
            new ArrayList<>()
        );
    }

    public void stackJobLogSaveFailureResult(
        Exception exception,
        JobDetailLog jobDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {
        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String detailLogFileName = unSavedLogFileFactory.createUnSavedLogFileNameWithExtension(
            jobDetailLog.getClass().getSimpleName() + "_" + jobDetailLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(detailLogFileName, jobDetailLog));

        String executionTimeLogFileName = unSavedLogFileFactory.createUnSavedLogFileNameWithExtension(
            executionTimeLog.getClass().getSimpleName() + "_" + executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(executionTimeLogFileName, executionTimeLog));

        stackLogSaveResult(
            createLogsSaveFailureMessage(
                BatchProcessType.JOB,
                exception,
                detailLogFileName,
                executionTimeLogFileName
            ),
            unSavedLogFiles
        );
    }

    public void stackStepLogSaveFailureResult(
        Exception exception,
        StepDetailLog stepDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {

        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String detailLogFileName = unSavedLogFileFactory.createUnSavedLogFileNameWithExtension(
            stepDetailLog.getClass().getSimpleName() + "_" + stepDetailLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(detailLogFileName, stepDetailLog));

        String executionTimeLogFileName = unSavedLogFileFactory.createUnSavedLogFileNameWithExtension(
            executionTimeLog.getClass().getSimpleName() + "_" + executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(executionTimeLogFileName, executionTimeLog));

        stackLogSaveResult(
            createLogsSaveFailureMessage(
                BatchProcessType.STEP,
                exception,
                detailLogFileName,
                executionTimeLogFileName
            ),
            unSavedLogFiles
        );
    }

    public void stackTaskLogSaveFailureResult(
        Exception exception,
        GithubApiLog githubApiLog,
        ExecutionTimeLog executionTimeLog
    ) {

        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String detailLogFileName = unSavedLogFileFactory.createUnSavedLogFileNameWithExtension(
            githubApiLog.getClass().getSimpleName() + "_" + githubApiLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(detailLogFileName, githubApiLog));

        String executionTimeLogFileName = unSavedLogFileFactory.createUnSavedLogFileNameWithExtension(
            executionTimeLog.getClass().getSimpleName() + "_" + executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(executionTimeLogFileName, executionTimeLog));

        stackLogSaveResult(
            createLogsSaveFailureMessage(
                BatchProcessType.TASK,
                exception,
                detailLogFileName,
                executionTimeLogFileName
            ),
            unSavedLogFiles
        );
    }

    private <T> void stackLogSaveResult(String message, List<UnSavedLogFile> unSavedLogFiles) {

        List<UnSavedLogFile> newUnSavedLogFiles = unSavedLogFileFactory.transformUnSavedLogFileByExcelMaxCellTextSize(unSavedLogFiles);

        logSaveResultStack.add(new LogSaveResult(message, newUnSavedLogFiles));
    }
}
