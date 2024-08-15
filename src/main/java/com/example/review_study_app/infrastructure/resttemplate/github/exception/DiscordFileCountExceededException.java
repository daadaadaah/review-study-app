package com.example.review_study_app.infrastructure.resttemplate.github.exception;

import static com.example.review_study_app.infrastructure.resttemplate.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_FILE_COUNT;

/**
 * DiscordMessageLengthExceededException 는 디스코드의 파일 갯수 한도량 초과할 때 발생하는 예외이다.
 */
public class DiscordFileCountExceededException extends RuntimeException {

    public DiscordFileCountExceededException(int currentDiscordFileCount) {
        super(String.format("디스코드로 전송할 파일 갯수는 %d 개를 초과할 수 없습니다. currentDiscordFileCount=%d", MAX_DISCORD_FILE_COUNT, currentDiscordFileCount));
    }
}
