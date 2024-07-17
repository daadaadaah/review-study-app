package com.example.review_study_app.github;

import java.util.List;

public interface GithubIssueService {

    NewLabelName createNewLabel(int year, int weekNumber) throws Exception;

    boolean isWeekNumberLabelPresent(String labelName);

    NewIssue createNewIssue(int currentYear, int currentWeekNumber, String memberFullName, String memberGithubName) throws Exception;

    List<IssueToClose> getIssuesToClose(String labelNameToClose) throws Exception;

    void closeIssue(int issueNumber) throws Exception;
}