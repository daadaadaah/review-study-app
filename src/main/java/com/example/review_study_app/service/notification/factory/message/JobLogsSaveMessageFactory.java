package com.example.review_study_app.service.notification.factory.message;

import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.notification.DiscordNotificationService;
import java.util.UUID;

/**
 * JobLogsSaveMessageFactory는 Job 배치 프로세스 로그 저장시, 상황별 알리 메시지를 만드는 팩토리 클래스이다.
 * 메시지 생성과 사용을 디커플링 시켜서 관심사 분리를 통해 로직을 단순화하고 싶어서 만들었다.
 */

public class JobLogsSaveMessageFactory {

    public static String createJobLogsSaveSuccessMessage(UUID jobId) {
        return String.format(
            "%sJob 로그 저장 성공 : jobId=%s %s",
            DiscordNotificationService.EMOJI_CONGRATS,
            jobId,
            DiscordNotificationService.EMOJI_CONGRATS
        );
    }

    public static String createJobLogsSaveDetailLogFailureMessage(
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

    public static String createJobLogsSaveRollbackSuccessMessage(
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

    public static String createJobLogsSaveRollbackFailureMessage(
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

    public static String createJobLogsSaveUnknownFailureMessage(
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
}
