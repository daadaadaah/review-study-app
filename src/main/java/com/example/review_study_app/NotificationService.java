package com.example.review_study_app;

public interface NotificationService {

    boolean sendMessage(String message);

    /** 라벨 생성 **/
    String createNewLabelCreationSuccessMessage(String weekNumberLabelName);

    String createNewLabelCreationFailureMessage(String weekNumberLabelName, Exception exception);

    /** 이슈 생성 **/
    String createNewIssueCreationSuccessMessage(String weekNumberLabelName, GithubApiSuccessResult githubApiSuccessResult);

    String createNewIssueCreationFailureMessage(String weekNumberLabelName, GithubApiFailureResult githubApiFailureResult);

    /** 이슈 Close **/
    String createIssueFetchFailureMessage(String weekNumberLabelName, Exception exception);

    String createEmptyIssuesToCloseMessage(String weekNumberLabelName);

    String createIssueCloseSuccessMessage(String weekNumberLabelName, GithubApiSuccessResult githubApiSuccessResult);

    String createIssueCloseFailureMessage(String weekNumberLabelName, GithubApiFailureResult githubApiFailureResult);
}
