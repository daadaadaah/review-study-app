package com.example.review_study_app.repository.log.exception;

public class SaveLogFailException extends RuntimeException {

    private final String log;

    public SaveLogFailException(Throwable cause, String log) {
        super(cause);
        this.log = log;
    }

    public String getData() {
        return log;
    }
}
