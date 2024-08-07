package com.example.review_study_app.repository.log.exception;

/**
 * GoogleSheetsTransactionException 은 Google Sheets와의 트랜잭션 중 발생하는 예외이다.
 * 이 예외는 특정 Google Sheets 범위를 롤백해야 할 필요가 있을 때 발생한다.
 *
 */

public class GoogleSheetsTransactionException extends RuntimeException {

    private final String googleSheetsRollbackRange;

    public GoogleSheetsTransactionException(Throwable cause, String googleSheetsRollbackRange) {
        super(cause);
        this.googleSheetsRollbackRange = googleSheetsRollbackRange;
    }

    public String getGoogleSheetsRollbackRange() {
        return googleSheetsRollbackRange;
    }
}
