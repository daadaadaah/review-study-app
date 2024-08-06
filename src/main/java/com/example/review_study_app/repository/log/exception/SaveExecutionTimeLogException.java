package com.example.review_study_app.repository.log.exception;

public class SaveExecutionTimeLogException extends RuntimeException {

    private final String rangeToRemoved;

    public SaveExecutionTimeLogException(Throwable cause, String rangeToRemoved) {
        super(cause);
        this.rangeToRemoved = rangeToRemoved;
    }

    public String getRangeToRemoved(){
        return rangeToRemoved;
    }
}
