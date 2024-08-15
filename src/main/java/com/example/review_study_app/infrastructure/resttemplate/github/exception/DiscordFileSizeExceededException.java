package com.example.review_study_app.infrastructure.resttemplate.github.exception;

import static com.example.review_study_app.infrastructure.resttemplate.discord.DiscordRestTemplateHttpClient.MAX_DISCORD_FILE_SIZE_MB;

/**
 * DiscordMessageLengthExceededException 는 디스코드의 파일 크기 한도량 초과할 때 발생하는 예외이다.
 */
public class DiscordFileSizeExceededException extends RuntimeException {

    public DiscordFileSizeExceededException(long currentDiscordFileSize) {
        super(String.format("디스코드로 전송할 파일 크기는 %d MB를 초과할 수 없습니다. currentDiscordFileSize=%d", MAX_DISCORD_FILE_SIZE_MB, currentDiscordFileSize));
    }
}
