package com.example.review_study_app.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * RetryAspect 는 @Retryable 어노테이션이 붙은 메서드와 그것의 클래스명을 RetryContext에 지정해주는 클래스이다.
 * 이는 어떤 클래스의 어떤 메서드가 재시도 되는지 파악하기 위해서 만들었다.
 */
@Slf4j
@Aspect
@Component
public class RetryAspect {

    @Before("@annotation(org.springframework.retry.annotation.Retryable)")
    public void beforeRetry(JoinPoint joinPoint) {
        RetryContext context = RetrySynchronizationManager.getContext();

        if (context != null) {
            context.setAttribute("className", joinPoint.getTarget().getClass().getName());
            context.setAttribute("methodName", joinPoint.getSignature().getName());
        }
    }
}
