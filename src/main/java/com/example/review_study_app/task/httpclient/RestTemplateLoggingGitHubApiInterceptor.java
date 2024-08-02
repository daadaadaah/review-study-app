package com.example.review_study_app.task.httpclient;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.common.service.log.LogService;
import com.example.review_study_app.common.service.log.dto.SaveTaskLogDto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
public class RestTemplateLoggingGitHubApiInterceptor implements ClientHttpRequestInterceptor {

    private final LogService logService;

    @Autowired
    public RestTemplateLoggingGitHubApiInterceptor(
        LogService logService
    ) {
        this.logService = logService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();

        String url = request.getURI().toString();

        HttpHeaders requestHeaders = request.getHeaders();

        String requestHttpMethod = request.getMethod().name();

        String requestBody = new String(body, StandardCharsets.UTF_8);

        try {

            ClientHttpResponse response = execution.execute(request, body);

            long endTime = System.currentTimeMillis();

            if (url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요

                saveTaskLog(new SaveTaskLogDto(
                    BatchProcessStatus.COMPLETED,
                    "Task 수행 완료",
                    requestHttpMethod,
                    url,
                    requestHeaders,
                    requestBody,
                    response.getStatusCode().value(),
                    response.getHeaders(),
                    response.getBody().toString(),
                    startTime,
                    endTime
                ));
            }

            return response;
        } catch (RestClientResponseException restClientResponseException) {
            long endTime = System.currentTimeMillis();

            if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요

                saveTaskLog(new SaveTaskLogDto(
                    BatchProcessStatus.STOPPED,
                    "예외 발생 : "+restClientResponseException.getMessage(),
                    requestHttpMethod,
                    url,
                    requestHeaders,
                    requestBody,
                    restClientResponseException.getStatusCode().value(),
                    restClientResponseException.getResponseHeaders(),
                    restClientResponseException.getResponseBodyAsString(),
                    startTime,
                    endTime
                ));
            }

            throw restClientResponseException;
        } catch (Exception exception) {
            log.error("{}에서 예상치 못한 예외가 발생했습니다. exception={}", getClass().toString(), exception.getMessage());

            throw exception;
        }
    }

    private void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {
        logService.saveTaskLog(saveTaskLogDto);
    }
}
