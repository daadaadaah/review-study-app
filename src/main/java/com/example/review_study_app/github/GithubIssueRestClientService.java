package com.example.review_study_app.github;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.review_study_app.common.httpclient.RetryableException;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class GithubIssueRestClientService {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private RestClient restClient;

    @Autowired
    public GithubIssueRestClientService(
        RestClient.Builder restClient
    ) {
        this.restClient = restClient.build();
    }

    private static String createGithubApiUrl(String path) {
        return "https://api.github.com/repos/" + ReviewStudyInfo.REPOSITORY_NAME + "/" + path;
    }

    // 새로운 주차 Label 생성하는 함수
    @Retryable(
        backoff = @Backoff(delay = 2000),
        retryFor = RetryableException.class
    )
    public String createNewLabel(int year, int weekNumber) throws Exception {

        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        GithubLabelForNewLabel githubLabelForNewLabel = new GithubLabelForNewLabel(labelName, labelDescription, labelColor);

        NewGithubLabel newGithubLabel = restClient.post()
                                                    .uri(createGithubApiUrl("labels"))
                                                    .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN))
                                                    .contentType(APPLICATION_JSON)
                                                    .body(githubLabelForNewLabel)
                                                    .retrieve()
                                                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                                                        HttpStatusCode httpStatusCode = response.getStatusCode();

                                                        log.info("statusCode={}, headers={}", httpStatusCode, response.getHeaders());

                                                        if(httpStatusCode.value() == 429) {
                                                            throw new RetryableException(httpStatusCode.toString());
                                                        }
                                                    })
                                                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                                                        HttpStatusCode httpStatusCode = response.getStatusCode();

                                                        log.info("statusCode={}, headers={}", httpStatusCode, response.getHeaders());

                                                        throw new RetryableException(httpStatusCode.toString());
                                                    })
                                                    .body(NewGithubLabel.class);

        return newGithubLabel.name();
    }

    // 라벨 존재 여부 판단 함수
    @Retryable(
        retryFor = RetryableException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public boolean isWeekNumberLabelPresent(String labelName) {
        try {
            NewGithubLabel newGithubLabel = restClient.get()
                .uri(createGithubApiUrl("labels/"+labelName))
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();

                    log.info("statusCode={}, headers={}", httpStatusCode, response.getHeaders());

                    if(httpStatusCode.value() == 404) {
                        throw new NotFoundException(httpStatusCode.toString());
                    }

                    if(httpStatusCode.value() == 429) {
                        throw new RetryableException(httpStatusCode.toString());
                    }
                })
                .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                    HttpStatusCode httpStatusCode = response.getStatusCode();

                    log.info("statusCode={}, headers={}", httpStatusCode, response.getHeaders());

                    throw new RetryableException(httpStatusCode.toString());
                })
                .body(NewGithubLabel.class);

            log.info("라벨이 존재합니다. labelName = {}", newGithubLabel);

            return true;
        } catch (NotFoundException notFoundException) {
            log.warn("라벨이 존재하지 않습니다. exception = {}, labelName = {}", notFoundException.getMessage(), labelName);

            return false;
        } catch (RetryableException retryableException) {
            log.error("현재 github 접속이 어려운 상태 입니다. exception = {}, labelName = {}", retryableException.getMessage(), labelName);

            throw retryableException;
        } catch (Exception exception) {
            log.error(" exception = {}, labelName = {}", exception.getMessage(), labelName);

            throw exception;
        }
    }

    // 새로운 주간회고 Issue 생성하는 함수
    public NewGithubIssue createNewIssue(
        int currentYear,
        int currentWeekNumber,
        String memberFullName,
        String memberGithubName
    ) {

        String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(currentYear, currentWeekNumber, memberFullName);

        String issueBody = ReviewStudyInfo.WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE;

        String thisWeekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        List<String> assignees = Arrays.asList(memberGithubName);

        List<String> labels =  Arrays.asList(thisWeekNumberLabelName);

        GithubIssueForNewIssue githubIssueForNewIssue = new GithubIssueForNewIssue(issueTitle, issueBody, assignees, labels);

        NewGithubIssue newGithubIssue = restClient.post()
            .uri(createGithubApiUrl("issues"))
            .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN))
            .contentType(APPLICATION_JSON)
            .body(githubIssueForNewIssue)
            .retrieve()
            .body(NewGithubIssue.class);

        return newGithubIssue;
    }
}
