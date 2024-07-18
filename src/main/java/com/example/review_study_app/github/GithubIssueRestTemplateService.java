package com.example.review_study_app.github;

import com.example.review_study_app.common.httpclient.MyHttpRequest;
import com.example.review_study_app.common.httpclient.MyHttpResponse;
import com.example.review_study_app.common.httpclient.RestTemplateHttpClient;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GithubIssueRestTemplateService implements GithubIssueService {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private RestTemplateHttpClient restTemplateHttpClient;

    private GithubApiMapper githubApiMapper;

    @Autowired
    public GithubIssueRestTemplateService(
        RestTemplateHttpClient restTemplateHttpClient,
        GithubApiMapper githubApiMapper
    ) {
        this.restTemplateHttpClient = restTemplateHttpClient;
        this.githubApiMapper = githubApiMapper;
    }

    private static String createGithubApiUrl(String path) {
        return "https://api.github.com/repos/" + ReviewStudyInfo.REPOSITORY_NAME + "/" + path;
    }

    private HttpHeaders createCommonHttpHeaders() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; utf-8");
        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN));

        return httpHeaders;
    }

    // 새로운 주차 Label 생성하는 함수
    public NewLabelName createNewLabel(int year, int weekNumber) throws Exception {
        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        String url = createGithubApiUrl("labels");

        HttpHeaders httpHeaders = createCommonHttpHeaders();

        LabelCreateForm labelCreateForm = new LabelCreateForm(labelName, labelDescription, labelColor);

        MyHttpRequest request = new MyHttpRequest(url, httpHeaders, labelCreateForm);

        MyHttpResponse response = restTemplateHttpClient.post(request);

        NewLabelName newLabelName = githubApiMapper.extractNewLabelNameFromResponseBody(response.body());

        return newLabelName;
    }

    // 라벨 존재 여부 판단 함수
    // HTTP response status code 목록 (참고 : https://docs.github.com/en/rest/issues/labels?apiVersion=2022-11-28#get-a-label--status-codes)
    public boolean isWeekNumberLabelPresent(String labelName) {
        try {
            String url = createGithubApiUrl("labels/"+labelName);

            HttpHeaders httpHeaders = createCommonHttpHeaders();

            MyHttpRequest request = new MyHttpRequest(url, httpHeaders, null);

            MyHttpResponse response = restTemplateHttpClient.get(request);

            return true;
        } catch (Exception exception) {

            log.warn("예외가 발생했습니다. exception = {}, labelName = {}", exception.getMessage(), labelName);

            return false;
        }
    }

    // 새로운 주간회고 Issue 생성하는 함수
    public NewIssue createNewIssue(
        int currentYear,
        int currentWeekNumber,
        String memberFullName,
        String memberGithubName
    ) throws Exception {
        String url = createGithubApiUrl("issues");

        HttpHeaders httpHeaders = createCommonHttpHeaders();

        String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(currentYear, currentWeekNumber, memberFullName);

        String issueBody = ReviewStudyInfo.WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE;

        String thisWeekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        List<String> assignees = Arrays.asList(memberGithubName);

        List<String> labels =  Arrays.asList(thisWeekNumberLabelName);

        IssueCreateForm issueCreateForm = new IssueCreateForm(
            issueTitle,
            issueBody,
            assignees,
            labels
        );

        MyHttpRequest request = new MyHttpRequest(url, httpHeaders, issueCreateForm);

        MyHttpResponse response = restTemplateHttpClient.post(request);

        NewIssue newIssue = githubApiMapper.extractNewIssueFromResponseBody(response.body());

        return newIssue;
    }

    // Close 할 이슈 목록 가져오는 함수
    public List<IssueToClose> getIssuesToClose(String labelNameToClose) throws Exception {
        // 기본 30개, 최대 100개
        String url = createGithubApiUrl("issues?state=open&labels="+labelNameToClose);

        HttpHeaders httpHeaders = createCommonHttpHeaders();

        MyHttpRequest request = new MyHttpRequest(url, httpHeaders, null);

        MyHttpResponse response = restTemplateHttpClient.get(request);

        List<IssueToClose> issuesToClose = githubApiMapper.extractIssueToClosListFromResponseBody(response.body());

        return issuesToClose;
    }

    // 이슈 close 하는 함수
    public void closeIssue(int issueNumber) throws Exception {
        String url = createGithubApiUrl("issues/"+issueNumber);

        HttpHeaders headers = createCommonHttpHeaders();

        IssueCloseForm issueCloseForm = new IssueCloseForm("close", "completed");

        MyHttpRequest request = new MyHttpRequest(url, headers, issueCloseForm);

        restTemplateHttpClient.patch(request);
    }
}
