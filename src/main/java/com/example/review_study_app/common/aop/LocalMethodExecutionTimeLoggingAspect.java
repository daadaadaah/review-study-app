package com.example.review_study_app.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Conditional(LocalEnvironmentCondition.class) // 해당 AOP는 Local 환경에서만 적용되도록 함! 디버깅할 때 사용하려고 만든거이기 때문에
public class LocalMethodExecutionTimeLoggingAspect {

    @Around("@annotation(LocalMethodExecutionTimeLogging)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        log.info("[local] "+joinPoint.getSignature().getName() + " executed in " + executionTime + "ms");

        return proceed;
    }
}
