package com.example.review_study_app.notification;

import static com.example.review_study_app.reviewstudy.ReviewStudyInfo.createIssueUrl;
import static com.example.review_study_app.reviewstudy.ReviewStudyInfo.createLabelUrl;

import com.example.review_study_app.github.GithubApiFailureResult;
import com.example.review_study_app.github.GithubApiSuccessResult;
import com.example.review_study_app.MyHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiscordNotificationService implements NotificationService {

    public static final String EMOJI_WARING = ":warning:";

    public static final String EMOJI_CONGRATS = ":tada:";

    public static final String EMOJI_EXCLAMATION_MARK = ":exclamation:";

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final MyHttpClient httpClient;

    public DiscordNotificationService(MyHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean sendMessage(String message) {
        try {
            MyHttpResponse response = httpClient.sendRequest(message);

            if (response.statusCode() != HttpStatus.NO_CONTENT.value()) {
                log.error("Discord 와의 통신 결과, 다음과 같은 에러가 발생했습니다. HTTPStateCode = {}", response.statusCode());
                return false;
            }

            log.info("Discord 로 메시지 전송이 성공했습니다.");

            return true;
        } catch (Exception exception) {
            log.error("Discord 로 메시지 전송이 실패했습니다. message = {}, exception = {}", message, exception.getMessage());
            return false;
        }
    }

    /** 라벨 생성 **/
    @Override
    public String createNewLabelCreationSuccessMessage(String weekNumberLabelName) {
        return EMOJI_CONGRATS+" 새로운 라벨(["+weekNumberLabelName+"]("+createLabelUrl()+")) 생성이 성공했습니다. "+ EMOJI_CONGRATS;
    }

    @Override
    public String createNewLabelCreationFailureMessage(String weekNumberLabelName,
        Exception exception) {
        return EMOJI_WARING+" 새로운 라벨(["+weekNumberLabelName+"]("+createLabelUrl()+")) 생성에 실패했습니다. "+ EMOJI_WARING+" \n"
            + " 에러 메시지 : "+exception.getMessage();
    }

    /** 이슈 생성 **/
    @Override
    public String createNewIssueCreationSuccessMessage(String weekNumberLabelName, GithubApiSuccessResult githubApiSuccessResult) {
        int issueNumber = githubApiSuccessResult.issueNumber();

        return EMOJI_CONGRATS +" ("+weekNumberLabelName+") "+ githubApiSuccessResult.issueTitle() + " 새로운 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+"))가 생성되었습니다. " + EMOJI_CONGRATS;
    }

    @Override
    public String createNewIssueCreationFailureMessage(String weekNumberLabelName, GithubApiFailureResult githubApiFailureResult) {
        return EMOJI_WARING +" ("+weekNumberLabelName+") "+ githubApiFailureResult.issueTitle()+" 새로운 이슈 생성이 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+githubApiFailureResult.errorMessage();
    }

    /** 이슈 Close **/
    @Override
    public String createIssueFetchFailureMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Close 할 이슈 목록 가져오는 것을 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+exception.getMessage();
    }

    @Override
    public String createEmptyIssuesToCloseMessage(String weekNumberLabelName) {
        return EMOJI_EXCLAMATION_MARK+" ("+weekNumberLabelName+") Close 할 이슈 목록이 없습니다. "+ EMOJI_EXCLAMATION_MARK;
    }

    @Override
    public String createIssueCloseSuccessMessage(String weekNumberLabelName, GithubApiSuccessResult githubApiSuccessResult) {
        int issueNumber = githubApiSuccessResult.issueNumber();

        return EMOJI_CONGRATS+" ("+weekNumberLabelName+") "+ githubApiSuccessResult.issueTitle()+" 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+"))가 Closed 되었습니다. "+ EMOJI_CONGRATS;
    }

    @Override
    public String createIssueCloseFailureMessage(String weekNumberLabelName, GithubApiFailureResult githubApiFailureResult) {
        int issueNumber = githubApiFailureResult.issueNumber();

        return EMOJI_WARING+" ("+weekNumberLabelName+") "+githubApiFailureResult.issueTitle()+" 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+")) Closed에 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+githubApiFailureResult.errorMessage();
    }
}