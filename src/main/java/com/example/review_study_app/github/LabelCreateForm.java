package com.example.review_study_app.github;

/**
 * LabelCreateForm 는 Github API를 통해 라벨을 생성할 때 필요한 데이터를 담는 클래스이다.
 */
public record LabelCreateForm(
    String name,
    String description,
    String color
) {

}
