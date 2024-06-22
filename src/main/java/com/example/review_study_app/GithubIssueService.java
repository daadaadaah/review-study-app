package com.example.review_study_app;

import java.io.IOException;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GithubIssueService {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private String REPOSITORY_NAME = ReviewStudyInfo.REPOSITORY_NAME;

    private GitHub github;

    private GHRepository repo;

    private void connectGithub() throws IOException {
        this.github = GitHub.connectUsingOAuth(GITHUB_OAUTH_ACCESS_TOKEN);
        this.repo = github.getRepository(REPOSITORY_NAME);
    }

    // 새로운 주차 Label 생성하는 함수
    public String createNewLabel(int year, int weekNumber) throws IOException {
        connectGithub();

        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        GHLabel ghLabel = repo.createLabel(labelName, labelColor, labelDescription);

        return ghLabel.getName();
    }
}