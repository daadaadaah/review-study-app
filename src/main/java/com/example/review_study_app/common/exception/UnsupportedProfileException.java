package com.example.review_study_app.common.exception;

/**
 * UnsupportedProfileException 는 지원하지 않는 profile 일 경우에 발생하는 예외이다.
 */

public class UnsupportedProfileException extends RuntimeException {

    public UnsupportedProfileException(String profile) {
        super("지원하는 환경 프로파일(예 : local, prod)이 아닙니다. profile="+profile);
    }
}
