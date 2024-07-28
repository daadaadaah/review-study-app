package com.example.review_study_app.scheduler.aop;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;
import static com.example.review_study_app.common.utils.MyDateUtils.getNow;

import com.example.review_study_app.common.service.notification.NotificationService;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private ZonedDateTime startTime;

    private final NotificationService notificationService;

    @Autowired
    public LoggingAspect(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Before("execution(* com.example.review_study_app.scheduler.ReviewStudySchedulerConfiguration.*(..))") // 일단은 Configuration 클래스만
    public void logBefore(JoinPoint joinPoint) {
        startTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        String now = getNow(startTime);

        log.info("Scheduler ({}) is starting. now = {}", joinPoint.getSignature().getName(), now);
    }

    @After("execution(* com.example.review_study_app.scheduler.ReviewStudySchedulerConfiguration.*(..))")
    public void logAfter(JoinPoint joinPoint) {
        ZonedDateTime endTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        String now = getNow(endTime);

        long durationMillis = java.time.Duration.between(startTime, endTime).toMillis();

        log.info("Scheduler ({}) has finished. now = {}, duration = {} ms", joinPoint.getSignature().getName(), now, durationMillis);

        String executionTimeMessage = notificationService.createExecutionTimeMessage(joinPoint.getSignature().getName(), durationMillis);

        notificationService.sendMessage(executionTimeMessage);
    }
}
