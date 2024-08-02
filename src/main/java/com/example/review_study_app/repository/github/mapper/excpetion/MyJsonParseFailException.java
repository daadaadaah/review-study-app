package com.example.review_study_app.repository.github.mapper.excpetion;

public class MyJsonParseFailException extends RuntimeException {

    public MyJsonParseFailException(Throwable throwable) {
        super(throwable);
    }
}
