package com.example.review_study_app.service.notification.factory.message;

import static com.example.review_study_app.infrastructure.github.GithubRestTemplateHttpClient.createIssueUrl;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_CONGRATS;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_EXCLAMATION_MARK;
import static com.example.review_study_app.service.notification.DiscordNotificationService.EMOJI_WARING;

import com.example.review_study_app.common.enums.ProfileType;
import com.example.review_study_app.service.github.vo.GithubIssueApiFailureResult;
import com.example.review_study_app.service.github.vo.GithubIssueApiSuccessResult;
import org.springframework.beans.factory.annotation.Autowired;

public class IssueCloseMessageFactory {

    private final ProfileType profileType; // TODO : 알림 메시지 만드는 팩토리 클래스를 static -> 일반 메서드로 바꾸고, DI 받는 형식으로 바꿀까?

    @Autowired
    public IssueCloseMessageFactory(ProfileType profileType) {
        this.profileType = profileType;
    }

    /** 이슈 Close **/
    public static String createIssueFetchFailureMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Close 할 이슈 목록 가져오는 것을 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+exception.getMessage();
    }

    public static String createEmptyIssuesToCloseMessage(String weekNumberLabelName) {
        return EMOJI_EXCLAMATION_MARK+" ("+weekNumberLabelName+") Close 할 이슈 목록이 없습니다. "+ EMOJI_EXCLAMATION_MARK;
    }

    public static String createUnexpectedIssueCloseFailureMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Issue Close Job 실패 (원인 : 예상치 못한 예외 발생)"+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+exception.getMessage();
    }

    public static String createIssueCloseSuccessMessage(ProfileType profileType, String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult) {
        int issueNumber = githubApiSuccessResult.issueNumber();

        return EMOJI_CONGRATS+" ("+weekNumberLabelName+") "+ githubApiSuccessResult.issueTitle()+" 이슈([#"+issueNumber+"]("+ createIssueUrl(profileType, issueNumber)+"))가 Closed 되었습니다. "+ EMOJI_CONGRATS;
    }

    public static String createIssueCloseFailureMessage(ProfileType profileType, String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult) {
        int issueNumber = githubIssueApiFailureResult.issueNumber();

        return EMOJI_WARING+" ("+weekNumberLabelName+") "+ githubIssueApiFailureResult.issueTitle()+" 이슈([#"+issueNumber+"]("+ createIssueUrl(profileType, issueNumber)+")) Closed에 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+ githubIssueApiFailureResult.errorMessage();
    }

}
