package com.example.review_study_app.github;

public record MyApiResult<T> (
    String identifier,
    boolean isSuccess,
    T value,
    Throwable error
) {

}
