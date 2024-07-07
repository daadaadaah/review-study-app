package com.example.review_study_app.github;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.example.review_study_app.common.httpclient.RetryableException;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
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
                                                    .body(NewGithubLabel.class);

        return newGithubLabel.name();
    }
}
