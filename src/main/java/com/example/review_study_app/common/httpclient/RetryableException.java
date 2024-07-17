package com.example.review_study_app.common.httpclient;

public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }
}
