package com.example.review_study_app.infrastructure.github.aop;

import com.example.review_study_app.service.log.helper.LogHelper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(value = 1) // 가장 바깥쪽에 감싸지도록
public class GithubRestTemplateHttpClientTaskIdManagementAspect {

    private final LogHelper logHelper;

    @Autowired
    public GithubRestTemplateHttpClientTaskIdManagementAspect(
        LogHelper logHelper
    ) {
        this.logHelper = logHelper;
    }

    @Around("execution(* com.example.review_study_app.infrastructure.github.GithubRestTemplateHttpClient.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            logHelper.setTaskId();

            Object result = joinPoint.proceed(); // 함수 실행

            return result;
        } catch (Exception exception) {

            throw exception;
        } finally {

            logHelper.clearTaskId();
        }
    }
}
