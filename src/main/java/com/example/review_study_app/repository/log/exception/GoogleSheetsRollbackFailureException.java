package com.example.review_study_app.repository.log.exception;

/**
 * GoogleSheetsRollbackFailureException 는 rollback 실패시 발생하는 예외 클래스이다.
 *
 * < 만든 이유 >
 * - rollback 실패시, 그에 맞는 알림 메시지를 보내기 위해서 분기점을 명시하기 위해
 */

public class GoogleSheetsRollbackFailureException extends RuntimeException {

    public GoogleSheetsRollbackFailureException(Throwable cause) {
        super(cause);
    }
}
