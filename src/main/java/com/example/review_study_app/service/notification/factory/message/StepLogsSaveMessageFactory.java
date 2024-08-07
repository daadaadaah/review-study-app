package com.example.review_study_app.service.notification.factory.message;

import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.notification.DiscordNotificationService;
import java.util.UUID;

/**
 * StepLogsSaveMessageFactory 는 Step 배치 프로세스 로그 저장시, 상황별 알리 메시지를 만드는 팩토리 클래스이다.
 * 메시지 생성과 사용을 디커플링 시켜서 관심사 분리를 통해 로직을 단순화하고 싶어서 만들었다.
 */

public class StepLogsSaveMessageFactory {

    public static String createStepLogsSaveSuccessMessage(UUID stepId) {
        return String.format(
            "%sStep 로그 저장 성공 : stepId=%s %s",
            DiscordNotificationService.EMOJI_CONGRATS,
            stepId,
            DiscordNotificationService.EMOJI_CONGRATS
        );
    }

    public static String createStepLogsSaveDetailLogFailureMessage(
        SaveDetailLogException exception,
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

    public static String createStepLogsSaveRollbackSuccessMessage(
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

    public static String createStepLogsSaveRollbackFailureMessage(
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

    public static String createStepLogsSaveUnKnownFailureMessage(
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
}
