package com.example.review_study_app.task.httpclient.aop;

import com.example.review_study_app.common.service.log.LogService;
import com.example.review_study_app.task.httpclient.dto.MyHttpRequest;
import com.example.review_study_app.task.httpclient.dto.MyHttpResponse;
import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import com.example.review_study_app.common.service.log.entity.GithubApiLog;
import com.example.review_study_app.common.service.log.LogHelper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;


@Slf4j
@Aspect
@Component
@Order(value = 2)
public class RestTemplateHttpClientTaskLoggingAspect {

    private final LogHelper logHelper;

    private final LogService logService;

    @Autowired
    public RestTemplateHttpClientTaskLoggingAspect(
        LogHelper logHelper,
        LogService logService
    ) {
        this.logHelper = logHelper;
        this.logService = logService;
    }

    /**
     * < Task의 경우, 구체적인 클레스를 지정한 이유 >
     * - Task는 특정 작업의 세부 단계를 의미하기 때문에 어떤 Task 냐에 따라 정말 다양한 데이터 구조를 가진다고 생각한다.
     * - 따라서, 재사용성 있게 범용성 있는 이름을 가진 클래스를 만드는 것보다, 세부 사항별로 Task Logging을 하는게, 좀더 단순하게 구현할 수 있을 것이라 생각했기 때문에, 구체적인 클래스를 지정했다.
     * - 만약, 추가적인 Task가 생긴다면, 그 Task의 이름을 가진 Aspect를 하나 더 만드는 방식으로 하는게 관심사 분리 관점에서 좋을 것 같다.
     */
    @Around("execution(* com.example.review_study_app.task.httpclient.RestTemplateHttpClient.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object[] args = joinPoint.getArgs();

        MyHttpRequest myHttpRequest = getMyHttpRequest(args);

        String url = myHttpRequest.url();

        String httpMethod = joinPoint.getSignature().getName().toUpperCase();

        String batchProcessName = findCallerMethodNameForRestTemplate();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            long endTime = System.currentTimeMillis();

            MyHttpResponse myHttpResponse = getMyHttpResponse(result);

            if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요

                GithubApiLog githubApiLog = logHelper.createGithubApiLog(
                    batchProcessName,
                    httpMethod,
                    myHttpRequest,
                    myHttpResponse,
                    startTime,
                    endTime
                );

                ExecutionTimeLog executionTimeLog = logHelper.createTaskExecutionTimeLog(
                    batchProcessName,
                    BatchProcessStatus.COMPLETED,
                    "Task 수행 완료",
                    githubApiLog.id(),
                    startTime,
                    endTime
                );

                saveTaskLog(githubApiLog, executionTimeLog);

                return result;
            }

            return result;
        } catch (RestClientResponseException restClientResponseException) {
            long endTime = System.currentTimeMillis();

            if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려.
                // url에 따라
                GithubApiLog githubApiLog = logHelper.createExceptionGithubApiLog(
                    batchProcessName,
                    httpMethod,
                    myHttpRequest,
                    restClientResponseException,
                    startTime,
                    endTime
                );

                ExecutionTimeLog executionTimeLog = logHelper.createTaskExecutionTimeLog(
                    batchProcessName,
                    BatchProcessStatus.STOPPED,
                    "예외 발생 : "+restClientResponseException.getMessage(),
                    githubApiLog.id(),
                    startTime,
                    endTime
                );

                saveTaskLog(githubApiLog, executionTimeLog);
            }

            throw restClientResponseException;
        } catch (Exception exception) {
            log.error("exception={}", exception.getMessage()); // TODO : 예외 처리 어떻게 할까?

            throw exception;
        }
    }

    private void saveTaskLog(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {
        logService.saveTaskLog(githubApiLog, executionTimeLog);
    }

    /**
     * findCallerMethodNameForRestTemplate 는 RestTemplate를 사용하는 메서드 이름을 찾는 함수이다.
     * 어떤 작업의 API 통신인지를 알기 위해 필요한 함수이다.
     */
    private String findCallerMethodNameForRestTemplate() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        for (StackTraceElement element : stackTraceElements) {
            if (element.getClassName().startsWith("com.example.review_study_app.task.httpclient.RestTemplateHttpClient")) { // Adjust the package name to match your application's package
                return element.getMethodName(); // RestTemplateHttpClient을 호출한 메서드명
            }
        }

        return ""; // TODO : 예외를 던져주는 것도 좋을 것 같음
    }

    private MyHttpResponse getMyHttpResponse(Object object) {
        if(object instanceof MyHttpResponse) {
            return (MyHttpResponse) object;
        }

        return null;
    }

    private MyHttpRequest getMyHttpRequest(Object[] args) {

        for (Object arg: args) {
            if(arg instanceof MyHttpRequest) {
                return (MyHttpRequest) arg;
            }
        }

        return null;
    }
}
