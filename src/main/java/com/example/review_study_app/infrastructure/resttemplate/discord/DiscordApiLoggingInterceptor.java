package com.example.review_study_app.infrastructure.resttemplate.discord;


import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.service.log.LogService;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.service.log.helper.LogHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 * DiscordApiLoggingInterceptor 는
 */

@Slf4j
@Component
public class DiscordApiLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final String REST_CLIENT = "RestTemplate";
    private final LogService logService;
    private final LogHelper logHelper;

    @Autowired
    public DiscordApiLoggingInterceptor(
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

        String batchProcessName = REST_CLIENT+requestHttpMethod;

        HttpHeaders requestHeaders = request.getHeaders();

        log.info("---------requestHeaders={}", requestHeaders);

        String requestBody = new String(body, StandardCharsets.UTF_8);

        try {

            ClientHttpResponse response = execution.execute(request, body);

            long endTime = System.currentTimeMillis();

            String responseBody = byteArrayInputStreamToString(response.getBody());



            log.info("---------responseHeaders={}", response.getHeaders());




//            saveTaskLog(new SaveTaskLogDto(
//                logHelper.getTaskId(),
//                logHelper.getStepId(),
//                batchProcessName,
//                BatchProcessStatus.COMPLETED,
//                "Task 수행 완료",
//                requestHttpMethod,
//                url,
//                requestHeaders,
//                requestBody,
//                response.getStatusCode().value(),
//                response.getHeaders(),
//                responseBody,
//                startTime,
//                endTime
//            ));

            return response;
        } catch (RestClientResponseException restClientResponseException) {
//            long endTime = System.currentTimeMillis();
//
//            saveTaskLog(new SaveTaskLogDto(
//                logHelper.getTaskId(),
//                logHelper.getStepId(),
//                batchProcessName,
//                BatchProcessStatus.STOPPED,
//                "예외 발생 : "+restClientResponseException.getMessage(),
//                requestHttpMethod,
//                url,
//                requestHeaders,
//                requestBody,
//                restClientResponseException.getStatusCode().value(),
//                restClientResponseException.getResponseHeaders(),
//                restClientResponseException.getResponseBodyAsString(),
//                startTime,
//                endTime
//            ));

            throw restClientResponseException;
        } catch (Exception exception) { // TODO : 이 catch 문 필요한가?
            log.error("{}에서 예상치 못한 예외가 발생했습니다. exception={}", getClass().toString(), exception.getMessage());

            throw exception;
        }
    }

    private String byteArrayInputStreamToString(InputStream inputStream) throws IOException {
        if(inputStream == null) {
            return "";
        }

        try {
            // ByteArrayInputStream을 사용하여 바이트를 읽음
            byte[] buffer = new byte[1024];
            int bytesRead;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // byte array를 얻고 String으로 변환 (UTF-8 인코딩 사용)
            String result = outputStream.toString(StandardCharsets.UTF_8.name());

            return result;
        } catch (IOException e) {
//            e.printStackTrace();
            throw  e;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

//    private void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {
//        logService.saveTaskLog(saveTaskLogDto);
//    }
}