package com.example.review_study_app.infrastructure.resttemplate.discord;

import com.example.review_study_app.infrastructure.resttemplate.common.dto.MyHttpRequest;
import com.example.review_study_app.infrastructure.resttemplate.common.dto.MyHttpResponse;
import com.example.review_study_app.infrastructure.resttemplate.github.exception.DiscordFileCountExceededException;
import com.example.review_study_app.infrastructure.resttemplate.github.exception.DiscordFileSizeExceededException;
import com.example.review_study_app.infrastructure.resttemplate.github.exception.DiscordMessageLengthExceededException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * DiscordRestTemplateHttpClient 는 RestTemplate으로 디스코드와의 통신하는 클라이언트이다.
 */

@Slf4j
@Component
public class DiscordRestTemplateHttpClient {

    // 참고 : https://discord.com/developers/docs/resources/message#create-message-jsonform-params
    public static final int MAX_DISCORD_MESSAGE_LENGTH = 2000;

    // 참고 : https://discord.com/developers/docs/resources/webhook#execute-webhook-jsonform-params
    public static final int MAX_DISCORD_FILE_COUNT = 10;

    // 참고 : https://support.discord.com/hc/ko/articles/25444343291031-File-Attachments-FAQ
    public static final long MAX_DISCORD_FILE_SIZE_MB = 25L * 1024 * 1024; // 25MB

    private final RestTemplate restTemplate;

    @Autowired
    public DiscordRestTemplateHttpClient(
        @Qualifier("discordRestTemplate") RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }

    public void validateMessage(String message) {

        if(message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("디스코드로 전송할 메시지가 없습니다.");
        }

        if(message.length() > MAX_DISCORD_MESSAGE_LENGTH) {
            throw new DiscordMessageLengthExceededException(message.length());
        }
    }

    public  <T> void validateFileCount(List<T> files) {
        if(files.size() > MAX_DISCORD_FILE_COUNT) {
            throw new DiscordFileCountExceededException(files.size());
        }
    }

    public  <T> void validateFileSize(ByteArrayResource resource) {
        if(resource == null || resource.contentLength() == 0) {
            throw new IllegalArgumentException("디스코드로 전송할 파일일 없습니다.");
        }

        if(resource.contentLength() > MAX_DISCORD_FILE_SIZE_MB) {
            throw new DiscordFileSizeExceededException(resource.contentLength());
        }
    }

    public MyHttpResponse post(MyHttpRequest request) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            request.url(),
            HttpMethod.POST,
            new HttpEntity<>(request.body(), request.headers()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}
