package com.example.review_study_app.github;

/**
 * GithubIssueToClosed 는 Github Issue를 Close 시키기 위해 필요한 데이터를 담고 있는 클래스이다.
 * Github Issue를 Close 시키고 싶으면, 객체를 다음과 같이 생성하면 된다.
 * 예 : new GithubIssueToClosed("close", "completed")
 */
public record GithubIssueToClosed(
    String state,
    String state_reason
) {

}
