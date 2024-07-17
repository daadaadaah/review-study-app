package com.example.review_study_app.github;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.review_study_app.common.httpclient.RetryableException;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 *
 * # 참고
 * https://www.baeldung.com/spring-boot-restclient
 * https://docs.github.com/ko/rest/issues/issues?apiVersion=2022-11-28
 */
@Slf4j
@Service
public class GithubIssueRestClientService implements GithubIssueService {

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
    public String createNewLabel(int year, int weekNumber) {

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
    @Async // TODO : 에러 메시지 : Invalid return type for async method (only Future and void supported): class com.example.review_study_app.github.NewGithubIssue
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

    // Close 할 이슈 목록 가져오는 함수
    @Retryable(
        retryFor = RetryableException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public List<NewGithubIssue> getIssuesToClose(String labelNameToClose) {
        return restClient.get()
            .uri(createGithubApiUrl("issues?per_page=10&state=open&labels="+labelNameToClose)) // TODO : 페이징 처리해야 됨.
            .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN))
            .retrieve()
            .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> { // TODO : 다른 상태 코드도 handle 해줘야 하나? 예 : 100번대 또는 300번대
                logHttpRequestAndResponse(request, response);
            })
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                HttpStatusCode httpStatusCode = response.getStatusCode();

                logHttpRequestAndResponse(request, response);

                /**
                 * 404 에러는 "issues1?state=open&labels=24년_27주차" 이렇게 잘못된 API 주소일 때 발생한다.
                 * 만약, 현재 open되어 있고, 특정 라벨이 달린 이슈에 해당하는 이슈가 없을 때에는 404가 아닌 정상 응답 200 으로 처리됨
                 */
                if(httpStatusCode.value() == 404) {
                    throw new NotFoundException(httpStatusCode.toString());
                }

                if(httpStatusCode.value() == 429) {
                    throw new RetryableException(httpStatusCode.toString());
                }
            })
            .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                HttpStatusCode httpStatusCode = response.getStatusCode();

                logHttpRequestAndResponse(request, response);

                throw new RetryableException(httpStatusCode.toString());
            })
            .body(new ParameterizedTypeReference<>() {});
    }

    // 이슈 close 하는 함수
    @Retryable(
        retryFor = RetryableException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    @Async
    public void closeIssue(int issueNumber) {
        restClient.patch()
            .uri(createGithubApiUrl("issues/"+issueNumber)) // 숫자 다르게 해서
            .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN))
            .body(new GithubIssueToClosed("open", "completed"))
            .retrieve()
            .onStatus(HttpStatusCode::is2xxSuccessful, (request, response) -> {
                logHttpRequestAndResponse(request, response);
            })
            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                HttpStatusCode httpStatusCode = response.getStatusCode();

                logHttpRequestAndResponse(request, response);

                if(httpStatusCode.value() == 404) { // 예 : 존재하지 않는 이슈 번호로 요청했을 때, 404 에러 발생함.
                    throw new NotFoundException(httpStatusCode.toString());
                }

                if(httpStatusCode.value() == 429) {
                    throw new RetryableException(httpStatusCode.toString());
                }
            })
            .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                HttpStatusCode httpStatusCode = response.getStatusCode();

                logHttpRequestAndResponse(request, response);

                throw new RetryableException(httpStatusCode.toString());
            })
            .toBodilessEntity();
    }

    private void logHttpRequestAndResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        HttpStatusCode httpStatusCode = response.getStatusCode();

        log.info("requestUri={}, requestHeaders={}, statusCode={}, responseHeaders={}", request.getURI(), request.getHeaders(), httpStatusCode, response.getHeaders());
    }
}
