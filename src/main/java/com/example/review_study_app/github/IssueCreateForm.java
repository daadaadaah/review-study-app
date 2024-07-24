package com.example.review_study_app.github;

import java.util.List;

/**
 * GithubIssueCreateForm 는 Github API를 통해 새로운 Github Issue를 생성할 때, body에 넣어주는 데이터를 담는 클래스이다.
 * 참고 : https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#create-an-issue
 */
public record IssueCreateForm(
    String title,
    String body,
    List<String> assignees,
//    int milestone, // 에러 테스트용
    List<String> labels
) {

}
