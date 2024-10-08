package com.example.review_study_app.service.github.vo;


/**
 * GithubApiTaskResult 는 Github API 통신하는 Task 관련 데이터를 담는 클래스이다.
 * @param isSuccess : 통신 성공 여부
 * @param taskResult : 통신 결과 (예 : GithubLabelApiSuccessResult)
 */
public record GithubApiTaskResult<T>(
    boolean isSuccess,

    T taskResult
) {
}
