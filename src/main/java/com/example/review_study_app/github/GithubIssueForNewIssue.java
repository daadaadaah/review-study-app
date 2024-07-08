package com.example.review_study_app.github;

import java.util.List;

/**
 * GithubIssueForNewIssue 는 새로운 Issue를 생성할 때, 필요한 데이터들을 담는 클래스이다.
 */
public record GithubIssueForNewIssue(
    String title,
    String body,
    List<String> assignees,
    List<String> labels
) {

}
