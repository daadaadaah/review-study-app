package com.example.review_study_app.common.httpclient.aop;

import com.example.review_study_app.service.log.helper.LogHelper;
import com.example.review_study_app.service.log.LogService;
import com.example.review_study_app.service.log.dto.SaveTaskLogDto;
import com.example.review_study_app.common.httpclient.dto.MyHttpRequest;
import com.example.review_study_app.common.httpclient.dto.MyHttpResponse;
import com.example.review_study_app.common.enums.BatchProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;


@Slf4j
@Aspect
@Component
@Order(value = 2)
public class RestTemplateHttpClientTaskLoggingAspect {

    private final LogService logService;

    private final LogHelper logHelper;
    @Autowired
    public RestTemplateHttpClientTaskLoggingAspect(
        LogService logService,
        LogHelper logHelper
    ) {
        this.logService = logService;
        this.logHelper = logHelper;
    }

    /**
     * < Task의 경우, 구체적인 클레스를 지정한 이유 >
     * - Task는 특정 작업의 세부 단계를 의미하기 때문에 어떤 Task 냐에 따라 정말 다양한 데이터 구조를 가진다고 생각한다.
     * - 따라서, 재사용성 있게 범용성 있는 이름을 가진 클래스를 만드는 것보다, 세부 사항별로 Task Logging을 하는게, 좀더 단순하게 구현할 수 있을 것이라 생각했기 때문에, 구체적인 클래스를 지정했다.
     * - 만약, 추가적인 Task가 생긴다면, 그 Task의 이름을 가진 Aspect를 하나 더 만드는 방식으로 하는게 관심사 분리 관점에서 좋을 것 같다.
     */
    @Around("execution(* com.example.review_study_app.common.httpclient.RestTemplateHttpClient.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object[] args = joinPoint.getArgs();

        MyHttpRequest myHttpRequest = getMyHttpRequest(args);

        String url = myHttpRequest.url();

        String httpMethod = joinPoint.getSignature().getName().toUpperCase();

        String batchProcessName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            long endTime = System.currentTimeMillis();

            MyHttpResponse myHttpResponse = getMyHttpResponse(result);

            if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려. 만약, Discord도 할 때, 클래스명 수정 필요

                saveTaskLog(new SaveTaskLogDto(
                    logHelper.getTaskId(),
                    logHelper.getStepId(),
                    batchProcessName,
                    BatchProcessStatus.COMPLETED,
                    "Task 수행 완료",
                    httpMethod,
                    myHttpRequest,
                    myHttpResponse,
                    startTime,
                    endTime
                ));

                return result;
            }

            return result;
        } catch (RestClientResponseException restClientResponseException) {
            long endTime = System.currentTimeMillis();

            if(url.contains("api.github.com/repos")) { // TODO : 일단 Github API만, Discord는 추후에 고려.

                saveTaskLog(new SaveTaskLogDto(
                    logHelper.getTaskId(),
                    logHelper.getStepId(),
                    batchProcessName,
                    BatchProcessStatus.STOPPED,
                    "예외 발생 : "+restClientResponseException.getMessage(),
                    httpMethod,
                    myHttpRequest,
                    restClientResponseException,
                    startTime,
                    endTime
                ));
            }

            throw restClientResponseException;
        } catch (Exception exception) {
            log.error("exception={}", exception.getMessage()); // TODO : 예외 처리 어떻게 할까?

            throw exception;
        }
    }

    private void saveTaskLog(SaveTaskLogDto saveTaskLogDto) {
        logService.saveTaskLog(saveTaskLogDto);
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
