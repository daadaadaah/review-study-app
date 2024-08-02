package com.example.review_study_app.service.github.domain;

public record GithubIssueApiFailureResult(
    Integer issueNumber, // int가 아닌 Integer롤 한 이유 : 새로운 이유 생성 실패일 경우, 이슈 넘버가 없으므로,
    String issueTitle,
    String errorMessage
) {

}
