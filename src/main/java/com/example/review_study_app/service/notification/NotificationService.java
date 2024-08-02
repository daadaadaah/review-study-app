package com.example.review_study_app.service.notification;

import com.example.review_study_app.service.github.domain.GithubIssueApiFailureResult;
import com.example.review_study_app.service.github.domain.GithubIssueApiSuccessResult;

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

    String createUnexpectedIssueCloseFailureMessage(String weekNumberLabelName, Exception exception);

    String createIssueCloseSuccessMessage(String weekNumberLabelName, GithubIssueApiSuccessResult githubApiSuccessResult);

    String createIssueCloseFailureMessage(String weekNumberLabelName, GithubIssueApiFailureResult githubIssueApiFailureResult);

    /** 로그 **/
    String createSchedulerLoggingMessage(String methodName, String startTime, String endTime, long totalExecutionTime);

    String createExecutionTimeMessage(String methodName, long totalExecutionTime);
}
