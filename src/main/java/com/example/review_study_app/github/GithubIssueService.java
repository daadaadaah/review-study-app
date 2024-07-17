package com.example.review_study_app.github;

import java.util.List;

public interface GithubIssueService {

    String createNewLabel(int year, int weekNumber);

    boolean isWeekNumberLabelPresent(String labelName);

    NewGithubIssue createNewIssue(int currentYear, int currentWeekNumber, String memberFullName, String memberGithubName);

    List<NewGithubIssue> getIssuesToClose(String labelNameToClose);

    void closeIssue(int issueNumber);
}