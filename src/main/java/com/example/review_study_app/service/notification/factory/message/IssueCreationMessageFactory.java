package com.example.review_study_app.service.notification.factory.message;

import static com.example.review_study_app.domain.ReviewStudyInfo.createIssueUrl;
import static com.example.review_study_app.domain.ReviewStudyInfo.createLabelUrl;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_CONGRATS;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_WARING;

import com.example.review_study_app.service.github.vo.GithubIssueApiFailureResult;
import com.example.review_study_app.service.github.vo.GithubIssueApiSuccessResult;

public class IssueCreationMessageFactory {

    public static String createIsWeekNumberLabelPresentFailMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Issue 생성 Job 실패 (원인 : 라벨(["+weekNumberLabelName+"]("+createLabelUrl()+"))  존재 여부 파악 실패) "+ EMOJI_WARING+" \n"
            + " 에러 메시지 : "+exception.getMessage();
    }

    public static String createUnexpectedIssueCreationFailureMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Issue 생성 Job 실패 (원인 : 예상치 못한 예외 발생)"+ EMOJI_WARING+" \n"
            + " 에러 메시지 : "+exception.getMessage();
    }

    public static String createNewIssueCreationSuccessMessage(String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult) {
        int issueNumber = githubApiSuccessResult.issueNumber();

        return EMOJI_CONGRATS +" ("+weekNumberLabelName+") "+ githubApiSuccessResult.issueTitle() + " 새로운 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+"))가 생성되었습니다. " + EMOJI_CONGRATS;
    }

    public static String createNewIssueCreationFailureMessage(String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult) {
        return EMOJI_WARING +" ("+weekNumberLabelName+") "+ githubIssueApiFailureResult.issueTitle()+" 새로운 이슈 생성이 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+ githubIssueApiFailureResult.errorMessage();
    }
}
