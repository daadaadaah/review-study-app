package com.example.review_study_app.service.notification.factory.message;

import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.repository.log.exception.GoogleSheetsRollbackFailureException;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.notification.DiscordNotificationService;
import java.util.UUID;

public class BatchProcessLogsSaveMessageFactory {

    public static String createLogsSaveSuccessMessage(
        BatchProcessType batchProcessType,
        UUID batchProcessId
    ) {
        return String.format(
            "%s %s 로그 저장 성공 : %sId=%s %s",
            DiscordNotificationService.EMOJI_CONGRATS,
            batchProcessType.name(),
            batchProcessType.name().toLowerCase(),
            batchProcessId,
            DiscordNotificationService.EMOJI_CONGRATS
        );
    }

    public static String createLogsSaveFailureMessage(
        BatchProcessType batchProcessType,
        Exception exception,
        String detailLogFileName,
        String executionTimeLogFileName
    ) {
        if(exception instanceof SaveDetailLogException) {
            return createLogsSaveDetailLogFailureMessage(batchProcessType, (SaveDetailLogException) exception, detailLogFileName, executionTimeLogFileName);
        }

        if(exception instanceof GoogleSheetsTransactionException) {
            return createLogsSaveRollbackSuccessMessage(batchProcessType, (GoogleSheetsTransactionException) exception, detailLogFileName, executionTimeLogFileName);
        }

        if(exception instanceof GoogleSheetsRollbackFailureException) {
            return createLogsSaveRollbackFailureMessage(batchProcessType, (GoogleSheetsRollbackFailureException) exception, detailLogFileName, executionTimeLogFileName);
        }

        return createLogsSaveUnknownFailureMessage(batchProcessType, exception, detailLogFileName, executionTimeLogFileName);
    }

    private static String createLogsSaveDetailLogFailureMessage(
        BatchProcessType batchProcessType,
        SaveDetailLogException exception,
        String detailLogFileName,
        String executionTimeLogFileName
    ) {
        return String.format(
            "%s %s 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- detailLog=%s \n"
                + "- executionTimeLog=%s",
            DiscordNotificationService.EMOJI_WARING,
            batchProcessType.name(),
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            detailLogFileName,
            executionTimeLogFileName
        );
    }

    private static String createLogsSaveRollbackSuccessMessage(
        BatchProcessType batchProcessType,
        GoogleSheetsTransactionException exception,
        String detailLogFileName,
        String executionTimeLogFileName
    ) {
        return String.format(
            "%s %s 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- detailLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- true (range : %s)",
            DiscordNotificationService.EMOJI_WARING,
            batchProcessType.name(),
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            detailLogFileName,
            executionTimeLogFileName,
            exception.getGoogleSheetsRollbackRange()
        );
    }

    private static String createLogsSaveRollbackFailureMessage(
        BatchProcessType batchProcessType,
        GoogleSheetsRollbackFailureException rollbackException,
        String detailLogFileName,
        String executionTimeLogFileName
    ) {
        return String.format(
            "%s %s 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- detailLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- false (range : %s) \n"
                + "- 예외 메시지 : %s",
            DiscordNotificationService.EMOJI_WARING,
            batchProcessType.name(),
            rollbackException.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getRollbackCause().getMessage(),
            detailLogFileName,
            executionTimeLogFileName,
            rollbackException.getGoogleSheetsRollbackRange(),
            rollbackException.getMessage()
        );
    }

    private static String createLogsSaveUnknownFailureMessage(
        BatchProcessType batchProcessType,
        Exception exception,
        String detailLogFileName,
        String executionTimeLogFileName
    ) {
        return String.format(
            "%s %s 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- detailLog=%s \n"
                + "- executionTimeLog=%s",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            batchProcessType.name(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            detailLogFileName,
            executionTimeLogFileName
        );
    }
}
