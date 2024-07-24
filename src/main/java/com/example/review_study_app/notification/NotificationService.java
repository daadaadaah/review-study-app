package com.example.review_study_app.notification;

import com.example.review_study_app.github.GithubIssueApiFailureResult;
import com.example.review_study_app.github.GithubIssueApiSuccessResult;

public interface NotificationService {

    boolean sendMessage(String message);

    /** 라벨 생성 **/
    String createNewLabelCreationSuccessMessage(String weekNumberLabelName);

    String createNewLabelCreationFailureMessage(String weekNumberLabelName, Exception exception);

    /** 이슈 생성 **/
    String createIsWeekNumberLabelPresentFailMessage(String weekNumberLabelName, Exception exception);

    String createUnexpectedIssueCreationFailureMessage(String weekNumberLabelName, Exception exception);

    String createNewIssueCreationSuccessMessage(String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult);

    String createNewIssueCreationFailureMessage(String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult);

    /** 이슈 Close **/
    String createIssueFetchFailureMessage(String weekNumberLabelName, Exception exception);

    String createEmptyIssuesToCloseMessage(String weekNumberLabelName);

    String createIssueCloseSuccessMessage(String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult);

    String createIssueCloseFailureMessage(String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult);

    /** 로그 **/
    String createExecutionTimeMessage(String methodName, long totalExecutionTime);
}
