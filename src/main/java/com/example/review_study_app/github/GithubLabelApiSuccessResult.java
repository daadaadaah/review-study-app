package com.example.review_study_app.github;

/**
 * GithubLabelApiSuccessResult 는 Github API 중 Label 과 관련된 통신 결과를 담는 클래스 이다.
 * @param labelName : 라벨 이름
 */
public record GithubLabelApiSuccessResult(
    String labelName
) {

}
