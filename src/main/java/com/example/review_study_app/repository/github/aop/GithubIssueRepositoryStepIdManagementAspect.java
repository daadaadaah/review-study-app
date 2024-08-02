package com.example.review_study_app.repository.github.aop;

import com.example.review_study_app.service.log.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(value = 1) // 가장 바깥쪽에 감싸지도록
public class GithubIssueRepositoryStepIdManagementAspect {

    private final LogHelper logHelper;

    @Autowired
    public GithubIssueRepositoryStepIdManagementAspect(
        LogHelper logHelper
    ) {
        this.logHelper = logHelper;
    }

    @Pointcut("target(com.example.review_study_app.repository.github.GithubIssueRepository)")
    public void targetImplementingInterface() {}

    @Around("targetImplementingInterface()")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            logHelper.setStepId();

            Object result = joinPoint.proceed(); // 함수 실행

            return result;
        } catch (Exception exception) {

            throw exception;
        } finally {

            logHelper.clearStepId();
        }
    }
}
