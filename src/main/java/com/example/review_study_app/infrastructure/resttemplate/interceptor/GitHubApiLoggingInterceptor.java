package com.example.review_study_app.infrastructure.resttemplate.interceptor;


import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.service.log.LogService;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * GitHubApiLoggingInterceptor 는
 */

@Slf4j
@Component
public class GitHubApiLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final LogService logService;
    private final LogHelper logHelper;

    @Autowired
    public GitHubApiLoggingInterceptor(
        LogService logService,
        LogHelper logHelper
    ) {
        this.logService = logService;
        this.logHelper = logHelper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        long startTime = System.currentTimeMillis();

        String url = request.getURI().toString();

        String requestHttpMethod = request.getMethod().name();

        String batchProcessName = requestHttpMethod+BatchProcessType.TASK.name();

        HttpHeaders requestHeaders= request.getHeaders();

        String requestBody = new String(body, StandardCharsets.UTF_8);

        try {

            ClientHttpResponse response = execution.execute(request, body);

            long endTime = System.currentTimeMillis();

            if (url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요

                saveTaskLog(new SaveTaskLogDto(
                    logHelper.getTaskId(),
                    logHelper.getStepId(),
                    batchProcessName,
                    BatchProcessStatus.COMPLETED,
                    "Task 수행 완료",
                    requestHttpMethod,
                    url,
                    requestHeaders,
                    requestBody,
                    response.getStatusCode().value(),
                    response.getHeaders(),
                    response.getBody() != null ? response.getBody().toString() : "",
                    startTime,
                    endTime
                ));
            }

            return response;
        } catch (RestClientResponseException restClientResponseException) {
            long endTime = System.currentTimeMillis();

            if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요

                saveTaskLog(new SaveTaskLogDto(
                    logHelper.getTaskId(),
                    logHelper.getStepId(),
                    batchProcessName,
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
        } catch (Exception exception) { // TODO : 이 catch 문 필요한가?
            log.error("{}에서 예상치 못한 예외가 발생했습니다. exception={}", getClass().toString(), exception.getMessage());

            throw exception;
        }
    }

    private void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {
        logService.saveTaskLog(saveTaskLogDto);
    }
}