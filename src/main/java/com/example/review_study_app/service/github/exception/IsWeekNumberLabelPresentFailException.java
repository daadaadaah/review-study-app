package com.example.review_study_app.service.github.exception;


/**
 * IsWeekNumberLabelPresentFailException 는 issue 생성 배치 작업시, 해당 issue 에 할당할 Label 이 있는지 파악에 실패했을 때 발생하는 예외이다.
 */
public class IsWeekNumberLabelPresentFailException extends RuntimeException{

    public IsWeekNumberLabelPresentFailException(Throwable cause) {
        super(cause);
    }
}
