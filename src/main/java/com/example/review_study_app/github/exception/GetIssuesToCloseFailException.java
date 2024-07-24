package com.example.review_study_app.github.exception;

/**
 * GetIssuesToCloseFailException 는 close 할 이슈 목록 가져오기를 실패했을 때 발생하는 에외이다.
 */
public class GetIssuesToCloseFailException extends RuntimeException {

    public GetIssuesToCloseFailException(Throwable throwable) {
        super(throwable);
    }
}
