package com.example.review_study_app.service.notification.exception;


/**
 * DiscordMessageLengthExceededException 는 디스코드의 메시지 한도량 초과할 때 발생하는 예외이다.
 */
public class DiscordMessageLengthExceededException extends RuntimeException {

    public DiscordMessageLengthExceededException(int maxDiscordMessageLength) { // 디스코드의 메시지 한도량이 변경될 수 있어서, 매개변수로 넣어 줌!
        super(String.format("디스코드로 전송할 메시지는 %s자를 초과할 수 없습니다.", maxDiscordMessageLength));
    }
}
