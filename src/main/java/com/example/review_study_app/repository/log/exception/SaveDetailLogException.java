package com.example.review_study_app.repository.log.exception;

/**
 * SaveDetailLogException 는 상세로그 저장 실패시 발생하는 예외 클래스이다.
 *
 * < 만든 이유 >
 * - 로그 저장 실패시, 케이스별 Discord 로 보내주는 알림 메시지가 다르다.
 * - 상세로그 저장 실패시를 명시적으로 분기하고 싶어서 만들었다.
 */

public class SaveDetailLogException extends RuntimeException {

    public SaveDetailLogException(Throwable cause) {
        super(cause);
    }
}
