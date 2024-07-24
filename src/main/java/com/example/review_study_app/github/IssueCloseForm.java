package com.example.review_study_app.github;

/**
 * IssueCloseForm는 Github API를 통해 이슈를 Close 시킬 때 필요한 데이터를 담는 클래스이다.
 * @param state
 * @param state_reason
 */
public record IssueCloseForm(
    String state,
    String state_reason
) {

}
