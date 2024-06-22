package com.example.review_study_app.github;

import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
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

    // 라벨 존재 여부 판단 함수
    public boolean isWeekNumberLabelPresent(String labelName) {
        try {
            connectGithub();

            repo.getLabel(labelName);
            log.info("라벨이 존재합니다. labelName = {}", labelName);

            return true;
        } catch (Exception exception) {
            log.warn("라벨이 존재하지 않습니다. exception = {}, labelName = {}", exception.getMessage(), labelName);
            return false;
        }
    }

    // 새로운 주간회고 Issue 생성하는 함수
    public GHIssue createNewIssue(
        int currentYear,
        int currentWeekNumber,
        String memberFullName,
        String memberGithubName
    ) throws IOException {
        connectGithub();

        String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(currentYear, currentWeekNumber, memberFullName);

        String issueBody = ReviewStudyInfo.WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE;

        String thisWeekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        return repo.createIssue(issueTitle)
            .assignee(memberGithubName)
            .label(thisWeekNumberLabelName)
            .body(issueBody)
            .create();
    }

    // Close 할 이슈 목록 가져오는 함수
    public List<GHIssue> getIssuesToClose(String labelNameToClose) throws IOException {
        connectGithub();

        return repo.getIssues(GHIssueState.OPEN)
            .stream()
            .filter(ghIssue -> ghIssue.getLabels().stream()
                .anyMatch(label -> label.getName().equals(labelNameToClose))
            )
            .toList();
    }

    // 이슈 close 하는 함수
    public void closeIssue(int issueNumber) throws IOException {
        connectGithub();

        repo.getIssue(issueNumber).close();
    }
}