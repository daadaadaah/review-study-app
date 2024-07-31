package com.example.review_study_app.step.aop;

import com.example.review_study_app.common.service.log.LogHelper;
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
public class GithubIssueServiceStepIdManagementAspect {

    private final LogHelper logHelper;

    @Autowired
    public GithubIssueServiceStepIdManagementAspect(
        LogHelper logHelper
    ) {
        this.logHelper = logHelper;
    }

    @Pointcut("target(com.example.review_study_app.step.GithubIssueServiceStep)")
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
