package com.example.review_study_app.task.mapper.excpetion;

public class MyJsonParseFailException extends RuntimeException {

    public MyJsonParseFailException(Throwable throwable) {
        super(throwable);
    }
}
