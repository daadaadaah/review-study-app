package com.example.review_study_app.infrastructure.resttemplate.github.exception;

import static com.example.review_study_app.infrastructure.resttemplate.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_MESSAGE_LENGTH;

/**
 * DiscordMessageLengthExceededException 는 디스코드의 메시지 글자수 한도량 초과할 때 발생하는 예외이다.
 */
public class DiscordMessageLengthExceededException extends RuntimeException {

    public DiscordMessageLengthExceededException(int currentMessageLength) {
        super(String.format("디스코드로 전송할 메시지는 %d자를 초과할 수 없습니다. currentMessageLength=%d", MAX_DISCORD_MESSAGE_LENGTH, currentMessageLength));
    }
}
