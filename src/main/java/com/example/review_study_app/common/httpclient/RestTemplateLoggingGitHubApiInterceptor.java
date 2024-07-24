package com.example.review_study_app.common.httpclient;


import com.example.review_study_app.log.GithubApiLog;
import com.example.review_study_app.log.LogGoogleSheetsRepository;
import com.example.review_study_app.log.LogHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;


/**
 * RestTemplateLoggingGitHubApiInterceptor 는 RestTemplate을 통해 외부 API와의 통신할 때, 요청/응답을 로깅하는 책임이 있는 클래스이다.
 *
 * // TODO : 응답에 따른 에러 핸들링 고려해보기 -> 단일 책임 원칙에 기반하여 다른 방법도 고려해 볼 수 있음
 */
@Slf4j
public class RestTemplateLoggingGitHubApiInterceptor implements ClientHttpRequestInterceptor {

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    private final LogHelper logHelper;

    @Autowired
    public RestTemplateLoggingGitHubApiInterceptor(
        LogGoogleSheetsRepository logGoogleSheetsRepository,
        LogHelper logHelper
    ) {
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
        this.logHelper =logHelper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        long start = System.currentTimeMillis();

        ClientHttpResponse response = execution.execute(request, body);

        long currentTimeMillis = System.currentTimeMillis();

        long responseTime = currentTimeMillis - start;

        String environment = logHelper.getEnvironment();

        String methodName = findCallerMethodNameForRestTemplate();

        String url = request.getURI().toString();

        String responseBody = new String(response.getBody().readAllBytes());

        if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요
            // url에 따라
            GithubApiLog githubApiLog = new GithubApiLog(
                logHelper.getId(currentTimeMillis),
                environment,
                methodName,
                request.getMethod().name(),
                url,
                request.getHeaders(),
                new String(body, StandardCharsets.UTF_8),
                response.getStatusCode().value(),
                response.getHeaders(),
                responseBody,
                responseTime,
                logHelper.getCreatedAt(currentTimeMillis)
            );

            logGoogleSheetsRepository.save(githubApiLog);
        }

        return response;
    }

    /**
     * findCallerMethodNameForRestTemplate 는 RestTemplate를 사용하는 메서드 이름을 찾는 함수이다.
     * 어떤 작업의 API 통신인지를 알기 위해 필요한 함수이다.
     */
    private String findCallerMethodNameForRestTemplate() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        int index = 0;

        for (StackTraceElement element : stackTraceElements) {

            if (element.getClassName().startsWith("com.example.review_study_app.common.httpclient.RestTemplateHttpClient")) { // Adjust the package name to match your application's package
                return stackTraceElements[index+1].getMethodName(); // RestTemplateHttpClient을 호출한 메서드명
            }

            index++;
        }

        return "";
    }
}
