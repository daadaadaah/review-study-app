package com.example.review_study_app.github.exception;

/**
 * IssuesToCloseIsEmptyException 는 Close 할 이슈 목록이 없을 때 발생하는 예외이다.
 */
public class IssuesToCloseIsEmptyException extends RuntimeException {

    public IssuesToCloseIsEmptyException() { // TODO : Close할 이슈의 기준인 Label 이름을 같이 던져줄까? 어떤 라벨이 붙은 issue 목록이 없는지 파악하기 위해
        super();
    }
}
