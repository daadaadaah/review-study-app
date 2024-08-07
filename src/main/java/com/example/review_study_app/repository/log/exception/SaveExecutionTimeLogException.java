package com.example.review_study_app.repository.log.exception;

/**
 * SaveExecutionTimeLogException 는 실행시간 로그 저장을 실패했을 때 발생하는 예외 클래스이다.
 *
 * < 만든 이유 >
 * - 실행시간 로그 저장이 실패한 경우, 앞서 저장된 각 배치 프로세스별 상세 로그의 rollback이 필요한대.
 * - 그 상황을 명시적으로 분기하고 싶어서 만들었다.
 */

public class SaveExecutionTimeLogException extends RuntimeException {
    public SaveExecutionTimeLogException(Throwable cause) {
        super(cause);
    }
}
