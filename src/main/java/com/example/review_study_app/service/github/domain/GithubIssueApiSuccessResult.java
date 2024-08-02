package com.example.review_study_app.service.github.domain;

public record GithubIssueApiSuccessResult(
    int issueNumber,
    String issueTitle
) {

}
