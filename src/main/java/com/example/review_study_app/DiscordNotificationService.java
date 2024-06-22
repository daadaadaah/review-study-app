package com.example.review_study_app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DiscordNotificationService implements NotificationService {

    public static final String EMOJI_WARING = ":warning:";

    public static final String EMOJI_CONGRATS = ":tada:";

    public static final String EMOJI_EXCLAMATION_MARK = ":exclamation:";

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    private final MyHttpClient httpClient;

    public DiscordNotificationService(MyHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean sendMessage(String message) {
        try {
            MyHttpResponse response = httpClient.sendRequest(message);

            if (response.statusCode() != HttpStatus.NO_CONTENT.value()) {
                log.error("Discord 와의 통신 결과, 다음과 같은 에러가 발생했습니다. HTTPStateCode = {}", response.statusCode());
                return false;
            }

            log.info("Discord 로 메시지 전송이 성공했습니다.");

            return true;
        } catch (Exception exception) {
            log.error("Discord 로 메시지 전송이 실패했습니다. message = {}, exception = {}", message, exception.getMessage());
            return false;
        }
    }
}
