package com.example.review_study_app.infrastructure.googlesheets.exception;

import com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason;


/**
 * CreateSheetsFailException 는 구글 시트 객체 생성 실패했을 때 발생한 예외이다.
 */
public class CreateSheetsFailException extends RuntimeException {

    private final CreateSheetsFailReason reason;

    public CreateSheetsFailException(CreateSheetsFailReason reason, Throwable cause) {
        super(cause);
        this.reason = reason;
    }

    public CreateSheetsFailReason getReason() {
        return reason;
    }
}
