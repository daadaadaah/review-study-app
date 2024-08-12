package com.example.review_study_app.service.log;

import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveDetailLogFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveRollbackFailureMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveRollbackSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveSuccessMessage;
import static com.example.review_study_app.service.notification.factory.message.GithubApiLogsSaveMessageFactory.createGithubApiLogsSaveUnKnownFailureMessage;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.repository.log.entity.DiscordApiLog;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.exception.GoogleSheetsRollbackFailureException;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class NoAlertLogDirectSaveGoogleSheetsService implements NoAlertLogService { // TODO : 일단 임시로 구현

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;
    private final LogHelper logHelper;

    @Autowired
    public NoAlertLogDirectSaveGoogleSheetsService(
        LogGoogleSheetsRepository logGoogleSheetsRepository,
        LogHelper logHelper
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
        this.logHelper = logHelper;
    }


    @Async("logSaveTaskExecutor")
    public void saveDiscordLog(SaveTaskLogDto saveTaskLogDto) {

        UUID stepId = saveTaskLogDto.stepId();

        long taskDetailLogId = saveTaskLogDto.endTime();

        long timeTaken = saveTaskLogDto.endTime() - saveTaskLogDto.startTime();

        DiscordApiLog discordApiLog = new DiscordApiLog(
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

        try {

            logGoogleSheetsRepository.saveDiscordApiLog(discordApiLog);

            log.info(createGithubApiLogsSaveSuccessMessage(stepId));
        } catch (SaveDetailLogException exception) { // 롤백 필요 없음

//            log.error(createGithubApiLogsSaveDetailLogFailureMessage(
//                exception,
//                discordApiLog,
//                executionTimeLog
//            ));

        } catch (GoogleSheetsTransactionException exception) { // 롤백 필요한 상황에서 롤백 성공한 경우

//            log.error(createGithubApiLogsSaveRollbackSuccessMessage(
//                exception,
//                discordApiLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (GoogleSheetsRollbackFailureException exception) { // 롤백 필요한 상황에서, 롤백 실패한 경우

//            log.error(createGithubApiLogsSaveRollbackFailureMessage(
//                exception,
//                discordApiLog,
//                executionTimeLog,
//                exception.getGoogleSheetsRollbackRange()
//            ));

        } catch (Exception exception) {

//            log.error(createGithubApiLogsSaveUnKnownFailureMessage(
//                exception,
//                discordApiLog,
//                executionTimeLog
//            ));
        }
    }
}
