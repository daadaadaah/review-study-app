package com.example.review_study_app.scheduler.aop;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;
import static com.example.review_study_app.common.utils.MyDateUtils.getNow;

import com.example.review_study_app.service.notification.NotificationService;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ReviewStudySchedulerLoggingAspect 는 스케줄러의 메소드 실행 전/후로 로깅 책임을 담당하는 AOP 클래스이다.
 * 이 클래스는 `ReviewStudyScheduler` 패키지 내의 모든 메소드에 대해 시작 시간, 종료 시간, 소요 시간을 기록하고, 실행 결과를 NotificationService 로 전송한다.
 */
@Aspect
@Component
@Slf4j
public class ReviewStudySchedulerLoggingAspect {

    private final NotificationService notificationService;

    @Autowired
    public ReviewStudySchedulerLoggingAspect(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Around("execution(* com.example.review_study_app.scheduler.ReviewStudyScheduler.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        ZonedDateTime startTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        String start = getNow(startTime);

        log.info("Scheduler ({}) is starting. now = {}", joinPoint.getSignature().getName(), start);

        Object result = joinPoint.proceed(); // 함수 실행

        ZonedDateTime endTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        String end = getNow(endTime);

        long durationMillis = java.time.Duration.between(startTime, endTime).toMillis();

        log.info("Scheduler ({}) has finished. now = {}, duration = {} ms", joinPoint.getSignature().getName(), end, durationMillis);

        String executionTimeMessage = notificationService.createSchedulerLoggingMessage(joinPoint.getSignature().getName(), start, end, durationMillis);

        notificationService.sendMessage(executionTimeMessage);

        return result;
    }
}
