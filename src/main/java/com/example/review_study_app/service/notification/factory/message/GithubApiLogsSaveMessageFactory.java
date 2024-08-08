package com.example.review_study_app.service.notification.factory.message;

import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.service.notification.DiscordNotificationService;
import java.util.UUID;

/**
 * GithubApiLogsSaveMessageFactory 는 배치 프로세스 중 Task 단계인 Github Api 통신 결과에 대한 로그 저장시, 상황별 알리 메시지를 만드는 팩토리 클래스이다.
 * 메시지 생성과 사용을 디커플링 시켜서 관심사 분리를 통해 로직을 단순화하고 싶어서 만들었다.
 */

public class GithubApiLogsSaveMessageFactory {

    public static String createGithubApiLogsSaveSuccessMessage(UUID taskId) {
        return String.format(
            "%sGithubApi 로그 저장 성공 : taskId=%s %s",
            DiscordNotificationService.EMOJI_CONGRATS,
            taskId,
            DiscordNotificationService.EMOJI_CONGRATS
        );
    }

    public static String createGithubApiLogsSaveDetailLogFailureMessage(
        SaveDetailLogException exception,
        GithubApiLog githubApiLog,
        ExecutionTimeLog executionTimeLog
    ) {
        return String.format(
            "%sGithubApi 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- githubApiLog=%s \n"
                + "- executionTimeLog=%s \n",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            githubApiLog,
            executionTimeLog
        );
    }

    public static String createGithubApiLogsSaveRollbackSuccessMessage(
        Exception exception,
        GithubApiLog githubApiLog,
        ExecutionTimeLog executionTimeLog,
        String range
    ){
        return String.format(
            "%sGithubApi 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- githubApiLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- %s (range : %s)",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            githubApiLog,
            executionTimeLog,
            "true",
            range
        );
    }

    public static String createGithubApiLogsSaveRollbackFailureMessage(
        Exception rollbackException,
        GithubApiLog githubApiLog,
        ExecutionTimeLog executionTimeLog,
        String range
    ) {
        return String.format(
            "%sGithubApi 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- githubApiLog=%s \n"
                + "- executionTimeLog=%s \n"
                + "<rollback 성공 여부> \n"
                + "- false (range : %s) \n"
                + "- 예외 메시지 : %s",
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            rollbackException.getMessage(),
            githubApiLog,
            executionTimeLog,
            range == null ? "" : range,
            rollbackException.getMessage()
        );
    }

    public static String createGithubApiLogsSaveUnKnownFailureMessage(
        Exception exception,
        GithubApiLog githubApiLog,
        ExecutionTimeLog executionTimeLog
    ) {
        return String.format(
            "%sGithubApi 로그 저장 실패(원인 : %s)%s\n"
                + "<예외 메시지> \n"
                + "- %s \n"
                + "<저장되지 않는 로그> \n"
                + "- githubApiLog=%s \n"
                + "- executionTimeLog=%s \n",
            DiscordNotificationService.EMOJI_WARING,
            exception.getClass().getSimpleName(),
            DiscordNotificationService.EMOJI_WARING,
            exception.getMessage(),
            githubApiLog,
            executionTimeLog
        );
    }
}