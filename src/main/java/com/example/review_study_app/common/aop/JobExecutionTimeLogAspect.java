package com.example.review_study_app.common.aop;


import com.example.review_study_app.github.JobResult;
import com.example.review_study_app.github.JobStatus;
import com.example.review_study_app.log.JobExecutionTimeLog;
import com.example.review_study_app.log.LogGoogleSheetsRepository;
import com.example.review_study_app.log.LogHelper;
import com.example.review_study_app.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class JobExecutionTimeLogAspect {

    private final LogHelper logHelper;

    private final NotificationService notificationService;

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    @Autowired
    public JobExecutionTimeLogAspect(
        LogHelper logHelper,
        NotificationService notificationService,
        LogGoogleSheetsRepository logGoogleSheetsRepository
    ) {
        this.logHelper = logHelper;
        this.notificationService = notificationService;
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
    }

    @Around("execution(* com.example.review_study_app.github.GithubJobFacade.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        long jobId = startTime;

        String environment = logHelper.getEnvironment();

        String createdAt = logHelper.getCreatedAt(startTime);

        String methodName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            JobResult jobResult = (JobResult) result;

            long endTime = System.currentTimeMillis();

            long timeTaken = endTime - startTime;

            JobExecutionTimeLog jobExecutionTimeLog = JobExecutionTimeLog.of(
                jobId,
                environment,
                jobResult,
                timeTaken,
                createdAt
            );

            logGoogleSheetsRepository.save(jobExecutionTimeLog);

            String executionTimeMessage = notificationService.createExecutionTimeMessage(methodName, timeTaken);

            notificationService.sendMessage(executionTimeMessage);

            return result;
        } catch (Exception exception) {
            long timeTaken = System.currentTimeMillis() - startTime;

            JobResult jobResult = new JobResult(
                methodName,
                JobStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(), // TODO : 현재 구조는 어떤 요청에 의해 예외가 발생했는지 모름! -> 개선 필요!
                0,
                0,
                null,
                0,
                null
            );

            JobExecutionTimeLog jobExecutionTimeLog = JobExecutionTimeLog.of(
                jobId,
                environment,
                jobResult,
                timeTaken,
                createdAt
            );

            logGoogleSheetsRepository.save(jobExecutionTimeLog);

            String executionTimeMessage = notificationService.createExecutionTimeMessage(methodName, timeTaken);

            notificationService.sendMessage(executionTimeMessage); // TODO : 예외도 같이 던져줘야 하나? 아니면 JobId 를 던져줘서 구글 시트로 확인할 수 있게 할까?

            throw exception;
        }
    }
}

