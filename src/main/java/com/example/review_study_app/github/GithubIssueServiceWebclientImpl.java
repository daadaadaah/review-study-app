package com.example.review_study_app.github;

import static com.example.review_study_app.common.json.MyJsonUtils.convertJsonToObject;
import static com.example.review_study_app.common.json.MyJsonUtils.extractFieldFromBody;
import static com.example.review_study_app.common.json.MyJsonUtils.extractNumbersFromIssuesBody;

import com.example.review_study_app.common.httpclient.MyHttpRequest;
import com.example.review_study_app.common.httpclient.MyHttpResponse;
import com.example.review_study_app.common.httpclient.WebClientHttpClient;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * 비인증된 사용자는 시간당 최대 60개의 요청을 할 수 있고, 개인 액세스 토큰을 사용하거나 GitHub 앱 또는 OAuth 앱을 통해 인증된 사용자는 시간당 최대 5,000개
 * https://docs.github.com/ko/rest/using-the-rest-api/rate-limits-for-the-rest-api?apiVersion=2022-11-28
 *
 */
@Slf4j
@Service
public class GithubIssueServiceWebclientImpl {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private String REPOSITORY_NAME = ReviewStudyInfo.REPOSITORY_NAME;

    private final WebClient webClient;

    private final WebClientHttpClient webClientHttpClient;

    @Autowired
    public GithubIssueServiceWebclientImpl(
        WebClientHttpClient webClientHttpClient,
        WebClient.Builder webClient
    ) {
        this.webClientHttpClient = webClientHttpClient;
        this.webClient = webClient.build();
    }

    private static String createGithubApiUrl(String path) {
        return "https://api.github.com/repos/" + ReviewStudyInfo.REPOSITORY_NAME + "/" + path;
    }

    private HttpHeaders createCommonHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; utf-8");
        headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN));

        return headers;
    }

    public String createLabel(int year, int weekNumber) {
        String url = createGithubApiUrl("labels");

        HttpHeaders headers = createCommonHttpHeaders();;

//        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelName = "테스트트트트1";

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        GithubLabelForNewLabel githubLabelForNewLabel = new GithubLabelForNewLabel(labelName, labelDescription, labelColor);

//        RetryBackoffSpec retrySpec = Retry.backoff(3, Duration.ofSeconds(3)) // 최대 3번 재시도, 3초 지연
//                                        .filter(throwable -> {
//                                            log.info("재시도하나?");
//
//                                            if (throwable instanceof WebClientResponseException) {
//                                                WebClientResponseException ex = (WebClientResponseException) throwable;
//                                                return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429 || ex.getStatusCode().value() == 422;
//                                            }
//                                            return false;
//                                        })
//                                        .doAfterRetry(retrySignal -> {
//                                            log.info("재시도 횟수: " + retrySignal.totalRetriesInARow());
//                                        })
//                                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
//                                            log.info("총 재시도 횟수: " + retrySignal.totalRetriesInARow());
//
//                                            log.info("에러럴 : "+retrySignal.failure().getMessage());
//                                            return retrySignal.failure();
//                                        }); // 실패했을 때, 어떻게 에러 뿜어내지? TODO : 422 를 임시로 retry 상황이라고 하고 테스트해보기
//
        Retry retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(3)) // vs backoff : 어떤 장점?
            .filter(throwable -> {
                log.info("재시도하나?");

                if (throwable instanceof WebClientResponseException) {
                    WebClientResponseException ex = (WebClientResponseException) throwable;
                    return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
                }
                return false;
            })
            .doBeforeRetry(retrySignal -> {
                log.info("[Retry 전] 재시도 횟수: " + retrySignal.totalRetries());
            })
            .doAfterRetry(retrySignal -> {
                // TODO : 나중에 의사결정하기 위해 몇번 재시도 만에 성공했는지 데이터화 해놓으면 좋을듯
                log.info("[Retry 후] 재시도 횟수: " + retrySignal.totalRetries());
            })
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                log.info("총 재시도 횟수: " + retrySignal.totalRetries());

                log.info("에러럴 : "+retrySignal.failure().getMessage());
                return retrySignal.failure();
            });

        MyHttpRequest request = new MyHttpRequest(url, headers, githubLabelForNewLabel);

        String body = webClientHttpClient.post(request).retryWhen(retrySpec).block();

        GithubLabel githubLabel = convertJsonToObject(body, GithubLabel.class);

        return githubLabel.name();
    }

    private int i = 0;

    public Mono<String> createIssue(int currentYear, int currentWeekNumber, String memberFullName, String memberGithubName) {

        String url = createGithubApiUrl("issues");

        HttpHeaders headers = createCommonHttpHeaders();

        String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(currentYear, currentWeekNumber, memberFullName);

        String issueBody = ReviewStudyInfo.WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE;

        String thisWeekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        List<String> assignees = Arrays.asList(memberGithubName);

        List<String> labels =  Arrays.asList(thisWeekNumberLabelName);

        // 임의의 실패 로직
        if(i == 3 || i == 2) {
            GithubIssueError githubIssueError = new GithubIssueError(
                issueTitle,
                issueBody,
                assignees,
                1,
                labels
            );

            MyHttpRequest request = new MyHttpRequest(url, headers, githubIssueError);
            i++;

            return webClientHttpClient.post(request);
        }

        GithubIssue githubIssue = new GithubIssue(
            issueTitle,
            issueBody,
            assignees,
            labels
        );

        MyHttpRequest request = new MyHttpRequest(url, headers, githubIssue);
        i++;

        return webClientHttpClient.post(request);
    }

    public List<Integer> getIssuesToClose(String labelNameToClose) throws Exception {
        // 기본 30개, 최대 100개
        String url = createGithubApiUrl("issues?state=open&labels="+labelNameToClose);

        HttpHeaders headers = createCommonHttpHeaders();

        MyHttpRequest request = new MyHttpRequest(url, headers, null);

        MyHttpResponse response = webClientHttpClient.get(request);

        List<Integer> issuesToClose = extractNumbersFromIssuesBody(response.body());

        return issuesToClose;
    }

    public Mono<String> closeIssue(int issueNumber) {

        String url = createGithubApiUrl("issues/"+issueNumber);

        HttpHeaders headers = createCommonHttpHeaders();

        GithubIssueToClosed githubIssueToClosed = new GithubIssueToClosed("close", "completed");

        MyHttpRequest request = new MyHttpRequest(url, headers, githubIssueToClosed);

        Retry retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(3)) // vs backoff : 어떤 장점?
            .filter(throwable -> {
                log.info("재시도하나?");

                if (throwable instanceof WebClientResponseException) {
                    WebClientResponseException ex = (WebClientResponseException) throwable;
                    return ex.getStatusCode().is5xxServerError() || ex.getStatusCode().value() == 429;
                }
                return false;
            })
            .doBeforeRetry(retrySignal -> {
                log.info("[Retry 전] 재시도 횟수: " + retrySignal.totalRetries());
            })
            .doAfterRetry(retrySignal -> {
                // TODO : 나중에 의사결정하기 위해 몇번 재시도 만에 성공했는지 데이터화 해놓으면 좋을듯
                log.info("[Retry 후] 재시도 횟수: " + retrySignal.totalRetries());
            })
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                log.info("총 재시도 횟수: " + retrySignal.totalRetries());

                log.info("에러럴 : "+retrySignal.failure().getMessage());
                return retrySignal.failure();
            });

        return webClientHttpClient.patch(request).retryWhen(retrySpec); // TODO : 에러 고의로 내봐서 테스트 해보기
    }
}
