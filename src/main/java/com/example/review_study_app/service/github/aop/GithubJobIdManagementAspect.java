package com.example.review_study_app.service.github.aop;

import com.example.review_study_app.service.log.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(value = 1) // 가장 바깥쪽에 감싸지도록
public class GithubJobIdManagementAspect {

    private final LogHelper logHelper;

    @Autowired
    public GithubJobIdManagementAspect(
        LogHelper logHelper
    ) {
        this.logHelper = logHelper;
    }

    /**
     *
     * GithubJob의 가장 바깥쪽에 감싸지는 AOP로서
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* com.example.review_study_app.job.GithubJob.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            logHelper.setJobId();

            Object result = joinPoint.proceed(); // 함수 실행

            return result;
        } catch (Exception exception) {

            throw exception;
        } finally {

            logHelper.clearJobId();
        }
    }
}
