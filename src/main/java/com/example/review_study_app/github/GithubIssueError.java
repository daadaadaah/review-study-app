package com.example.review_study_app.github;


import java.util.List;

public record GithubIssueError(
    String title,
    String body,
    List<String> assignees,
    int milestone, // 에러 테스트용
    List<String> labels
) {

}
