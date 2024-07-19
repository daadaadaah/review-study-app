package com.example.review_study_app.common.exception;

public class MyJsonParseFailException extends RuntimeException {

    public MyJsonParseFailException(Throwable throwable) {
        super(throwable);
    }
}
