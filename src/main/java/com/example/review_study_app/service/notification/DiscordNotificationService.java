package com.example.review_study_app.service.notification;

import static com.example.review_study_app.domain.ReviewStudyInfo.createIssueUrl;
import static com.example.review_study_app.domain.ReviewStudyInfo.createLabelUrl;

import com.example.review_study_app.infrastructure.resttemplate.discord.DiscordRestTemplateHttpClient;
import com.example.review_study_app.service.notification.dto.UnSavedLogFile;
import com.example.review_study_app.service.notification.dto.NotificationMessage;
import com.example.review_study_app.infrastructure.resttemplate.common.dto.MyHttpRequest;
import com.example.review_study_app.infrastructure.resttemplate.common.dto.MyHttpResponse;
import com.example.review_study_app.service.github.domain.GithubIssueApiFailureResult;
import com.example.review_study_app.service.github.domain.GithubIssueApiSuccessResult;
import com.example.review_study_app.service.notification.factory.file.UnSavedLogFileFactory;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
public class DiscordNotificationService implements NotificationService {

    public static final String EMOJI_WARING = ":warning:";

    public static final String EMOJI_CONGRATS = ":tada:";

    public static final String EMOJI_EXCLAMATION_MARK = ":exclamation:";

    public static final String EMOJI_CLOCK = ":alarm_clock:";

    @Value("${discord.webhook.url}")
    private String webhookUrl; // TODO : DiscordRestTemplateHttpClient 에 있는게 더 맞을 것 같긴함. 나중에 수정하기

    private final DiscordRestTemplateHttpClient discordRestTemplateHttpClient;

    private final UnSavedLogFileFactory unSavedLogFileFactory;

    public DiscordNotificationService(
        DiscordRestTemplateHttpClient discordRestTemplateHttpClient,
        UnSavedLogFileFactory unSavedLogFileFactory
    ) {
        this.discordRestTemplateHttpClient = discordRestTemplateHttpClient;
        this.unSavedLogFileFactory = unSavedLogFileFactory;
    }

    @Override
    public boolean sendMessage(String message) {

        discordRestTemplateHttpClient.validateMessage(message);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");

            MyHttpRequest request = new MyHttpRequest(webhookUrl, httpHeaders, new NotificationMessage(message));

            MyHttpResponse response = discordRestTemplateHttpClient.post(request);

            int statusCode = response.statusCode();

            if (statusCode != HttpStatus.NO_CONTENT.value() && statusCode != HttpStatus.OK.value()) {
                log.error("Discord 와의 통신 결과, 다음과 같은 에러가 발생했습니다. HTTPStateCode = {}", response.statusCode());
                return false;
            }

            log.info("Discord 로 메시지 전송이 성공했습니다. message = {}", message);

            return true;
        } catch (Exception exception) {
            log.error("Discord 로 메시지 전송이 실패했습니다. message = {}, exception = {}", message, exception.getMessage());
            return false;
        }
    }

    @Override
    public <T> boolean sendMessageWithFiles(String message, List<UnSavedLogFile> unSavedLogFiles) {

        if (unSavedLogFiles == null || unSavedLogFiles.isEmpty()) {
            return sendMessage(message);
        }

        discordRestTemplateHttpClient.validateMessage(message);

        discordRestTemplateHttpClient.validateFileCount(unSavedLogFiles);

        List<ByteArrayResource> fileResources = unSavedLogFileFactory.generateByteArrayResourcesFromUnSavedLogFile(unSavedLogFiles);

        for(ByteArrayResource fileResource: fileResources) {
            discordRestTemplateHttpClient.validateFileSize(fileResource); // TODO : 여기서 해주는게 맞나? 밑에서 반복문 1개로 하고, 초과되는 것만 뺴고 전송하는것도 방법일 수 있겠다
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("content", message);

            for (int index = 0; index < fileResources.size(); index++) { // TODO : 총 파일 크기수도 유효성 체크해야 하나?
                ByteArrayResource fileResource = fileResources.get(index);
                body.add("file" + index, fileResource);
            }

            MyHttpRequest request = new MyHttpRequest(webhookUrl, headers, body);

            MyHttpResponse response = discordRestTemplateHttpClient.post(request);

            int statusCode = response.statusCode();

            if (statusCode != HttpStatus.NO_CONTENT.value() && statusCode != HttpStatus.OK.value()) {
                log.error("Discord 와의 통신 결과, 다음과 같은 에러가 발생했습니다. HTTPStateCode = {}", response.statusCode());
                return false;
            }

            log.info("Discord 로 메시지 전송이 성공했습니다. message = {}", message);

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
    public String createIsWeekNumberLabelPresentFailMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Issue 생성 Job 실패 (원인 : 라벨(["+weekNumberLabelName+"]("+createLabelUrl()+"))  존재 여부 파악 실패) "+ EMOJI_WARING+" \n"
            + " 에러 메시지 : "+exception.getMessage();
    }

    @Override
    public String createUnexpectedIssueCreationFailureMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Issue 생성 Job 실패 (원인 : 예상치 못한 예외 발생)"+ EMOJI_WARING+" \n"
            + " 에러 메시지 : "+exception.getMessage();
    }

    @Override
    public String createNewIssueCreationSuccessMessage(String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult) {
        int issueNumber = githubApiSuccessResult.issueNumber();

        return EMOJI_CONGRATS +" ("+weekNumberLabelName+") "+ githubApiSuccessResult.issueTitle() + " 새로운 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+"))가 생성되었습니다. " + EMOJI_CONGRATS;
    }

    @Override
    public String createNewIssueCreationFailureMessage(String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult) {
        return EMOJI_WARING +" ("+weekNumberLabelName+") "+ githubIssueApiFailureResult.issueTitle()+" 새로운 이슈 생성이 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+ githubIssueApiFailureResult.errorMessage();
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
    public String createUnexpectedIssueCloseFailureMessage(String weekNumberLabelName, Exception exception) {
        return EMOJI_WARING+" ("+weekNumberLabelName+") Issue Close Job 실패 (원인 : 예상치 못한 예외 발생)"+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+exception.getMessage();
    }

    @Override
    public String createIssueCloseSuccessMessage(String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult) {
        int issueNumber = githubApiSuccessResult.issueNumber();

        return EMOJI_CONGRATS+" ("+weekNumberLabelName+") "+ githubApiSuccessResult.issueTitle()+" 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+"))가 Closed 되었습니다. "+ EMOJI_CONGRATS;
    }

    @Override
    public String createIssueCloseFailureMessage(String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult) {
        int issueNumber = githubIssueApiFailureResult.issueNumber();

        return EMOJI_WARING+" ("+weekNumberLabelName+") "+ githubIssueApiFailureResult.issueTitle()+" 이슈([#"+issueNumber+"]("+ createIssueUrl(issueNumber)+")) Closed에 실패했습니다. "+ EMOJI_WARING
            +"\n"
            + "에러 메시지 : "+ githubIssueApiFailureResult.errorMessage();
    }

    @Override
    public String createExecutionTimeMessage(String methodName, long totalExecutionTime) {
        return EMOJI_CLOCK + " (" +methodName+") 의 총 소요시간 : "+totalExecutionTime+" ms"+" "+EMOJI_CLOCK;
    }

    @Override
    public String createSchedulerLoggingMessage(String methodName, String startTime, String endTime, long totalExecutionTime) {
        return EMOJI_CLOCK +" (" +methodName+") : 시작 시간 - "+startTime+", 종료 시간 - "+endTime+", 총 소요시간 - "+totalExecutionTime+" ms, "+EMOJI_CLOCK;
    }
}
