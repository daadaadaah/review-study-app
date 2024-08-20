package com.example.review_study_app.service.notification;

import com.example.review_study_app.infrastructure.discord.DiscordRestTemplateHttpClient;
import com.example.review_study_app.service.notification.vo.UnSavedLogFile;
import com.example.review_study_app.service.notification.dto.NotificationMessage;
import com.example.review_study_app.common.dto.MyHttpResponse;
import com.example.review_study_app.service.notification.factory.file.UnSavedLogFileFactory;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
public class DiscordNotificationService implements NotificationService {

    public static final String EMOJI_WARING = ":warning:";

    public static final String EMOJI_CONGRATS = ":tada:";

    public static final String EMOJI_EXCLAMATION_MARK = ":exclamation:";

    public static final String EMOJI_CLOCK = ":alarm_clock:";

    private final DiscordRestTemplateHttpClient discordRestTemplateHttpClient;

    private final UnSavedLogFileFactory unSavedLogFileFactory;

    public DiscordNotificationService(
        DiscordRestTemplateHttpClient discordRestTemplateHttpClient,
        UnSavedLogFileFactory unSavedLogFileFactory
    ) {
        this.discordRestTemplateHttpClient = discordRestTemplateHttpClient;
        this.unSavedLogFileFactory = unSavedLogFileFactory;
    }

    @Override
    public boolean sendMessage(String message) {

        discordRestTemplateHttpClient.validateMessage(message);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Content-Type", "application/json; utf-8");

            MyHttpResponse response = discordRestTemplateHttpClient.post(httpHeaders, new NotificationMessage(message));

            int statusCode = response.statusCode();

            if (statusCode != HttpStatus.NO_CONTENT.value() && statusCode != HttpStatus.OK.value()) {
                log.error("Discord 와의 통신 결과, 다음과 같은 에러가 발생했습니다. HTTPStateCode = {}", response.statusCode());
                return false;
            }

            log.info("Discord 로 메시지 전송이 성공했습니다. message = {}", message);

            return true;
        } catch (Exception exception) {
            log.error("Discord 로 메시지 전송이 실패했습니다. message = {}, exception = {}", message, exception.getMessage());
            return false;
        }
    }

    @Override
    public <T> boolean sendMessageWithFiles(String message, List<UnSavedLogFile> unSavedLogFiles) {

        if (unSavedLogFiles == null || unSavedLogFiles.isEmpty()) {
            return sendMessage(message);
        }

        discordRestTemplateHttpClient.validateMessage(message);

        discordRestTemplateHttpClient.validateFileCount(unSavedLogFiles);

        List<ByteArrayResource> fileResources = unSavedLogFileFactory.generateByteArrayResourcesFromUnSavedLogFiles(unSavedLogFiles);

        for(ByteArrayResource fileResource: fileResources) {
            discordRestTemplateHttpClient.validateFileSize(fileResource); // TODO : 여기서 해주는게 맞나? 밑에서 반복문 1개로 하고, 초과되는 것만 뺴고 전송하는것도 방법일 수 있겠다
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("content", message);

            for (int index = 0; index < fileResources.size(); index++) { // TODO : 총 파일 크기수도 유효성 체크해야 하나?
                ByteArrayResource fileResource = fileResources.get(index);
                body.add("file" + index, fileResource);
            }

            MyHttpResponse response = discordRestTemplateHttpClient.post(headers, body);

            int statusCode = response.statusCode();

            if (statusCode != HttpStatus.NO_CONTENT.value() && statusCode != HttpStatus.OK.value()) {
                log.error("Discord 와의 통신 결과, 다음과 같은 에러가 발생했습니다. HTTPStateCode = {}", response.statusCode());
                return false;
            }

            log.info("Discord 로 메시지 전송이 성공했습니다. message = {}", message);

            return true;
        } catch (Exception exception) {
            log.error("Discord 로 메시지 전송이 실패했습니다. message = {}, exception = {}", message, exception.getMessage());
            return false;
        }
    }
}
