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
public class LogSaveDiscordNotificationFacade {

    private NotificationService notificationService;

    private final UnSavedLogFileFactory unSavedLogFileFactory;

    private final Stack<LogSaveResult> logSaveResultStack = new Stack<>();

    @Autowired
    public LogSaveDiscordNotificationFacade(
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
        logSaveResultStack.add(new LogSaveResult(
            createLogsSaveSuccessMessage(batchProcessType, batchProcessId),
            new ArrayList<>()
        ));
    }

    public void stackJobLogSaveFailureResult(
        Exception exception,
        JobDetailLog jobDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {
        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String jobDetailLogFileName = unSavedLogFileFactory.createFileNameWithExtension(
            jobDetailLog.getClass().getSimpleName() + "_" + jobDetailLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(jobDetailLogFileName, jobDetailLog));

        String jobExecutionTimeLogFileName = unSavedLogFileFactory.createFileNameWithExtension(
            executionTimeLog.getClass().getSimpleName() + "_" + executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(jobExecutionTimeLogFileName, executionTimeLog));

        logSaveResultStack.add(new LogSaveResult(
            createLogsSaveFailureMessage(
                BatchProcessType.JOB,
                exception,
                jobDetailLogFileName,
                jobExecutionTimeLogFileName
            ),
            unSavedLogFiles
        ));
    }

    public void stackStepLogSaveFailureResult(
        Exception exception,
        StepDetailLog stepDetailLog,
        ExecutionTimeLog executionTimeLog
    ) {

        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String jobDetailLogFileName = unSavedLogFileFactory.createFileNameWithExtension(
            stepDetailLog.getClass().getSimpleName() + "_" + stepDetailLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(jobDetailLogFileName, stepDetailLog));

        String jobExecutionTimeLogFileName = unSavedLogFileFactory.createFileNameWithExtension(
            executionTimeLog.getClass().getSimpleName() + "_" + executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(jobExecutionTimeLogFileName, executionTimeLog));

        logSaveResultStack.add(new LogSaveResult(
            createLogsSaveFailureMessage(
                BatchProcessType.STEP,
                exception,
                jobDetailLogFileName,
                jobExecutionTimeLogFileName
            ),
            unSavedLogFiles
        ));
    }


    public void stackTaskLogSaveFailureResult(
        Exception exception,
        GithubApiLog githubApiLog,
        ExecutionTimeLog executionTimeLog
    ) {

        List<UnSavedLogFile> unSavedLogFiles = new ArrayList<>();

        String jobDetailLogFileName = unSavedLogFileFactory.createFileNameWithExtension(
            githubApiLog.getClass().getSimpleName() + "_" + githubApiLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(jobDetailLogFileName, githubApiLog));

        String jobExecutionTimeLogFileName = unSavedLogFileFactory.createFileNameWithExtension(
            executionTimeLog.getClass().getSimpleName() + "_" + executionTimeLog.id());

        unSavedLogFiles.add(new UnSavedLogFile(jobExecutionTimeLogFileName, executionTimeLog));

        logSaveResultStack.add(new LogSaveResult(
            createLogsSaveFailureMessage(
                BatchProcessType.TASK,
                exception,
                jobDetailLogFileName,
                jobExecutionTimeLogFileName
            ),
            unSavedLogFiles
        ));
    }
}
