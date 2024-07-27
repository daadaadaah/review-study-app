package com.example.review_study_app.common.aop;


import com.example.review_study_app.common.utils.BatchProcessIdContext;
import com.example.review_study_app.github.JobResult;
import com.example.review_study_app.github.BatchProcessStatus;
import com.example.review_study_app.log.BatchProcessType;
import com.example.review_study_app.log.ExecutionTimeLog;
import com.example.review_study_app.log.JobDetailLog;
import com.example.review_study_app.log.LogGoogleSheetsRepository;
import com.example.review_study_app.log.LogHelper;
import com.example.review_study_app.notification.NotificationService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class JobLogAspect {

    private final LogHelper logHelper;

    private final NotificationService notificationService;

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    @Autowired
    public JobLogAspect(
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

        UUID uuid = UUID.randomUUID();

        BatchProcessIdContext.setJobId(uuid);

        String environment = logHelper.getEnvironment();

        String createdAt = logHelper.getCreatedAt(startTime);

        String methodName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            JobResult jobResult = (JobResult) result;

            long endTime = System.currentTimeMillis();

            long jodDetailLogId = endTime; // 그냥, endTime으로 넣어줄 수 있는데, jodDetailLogId 이 구체적으로 어떤 값이 명시하기 위해 이렇게 함

            long timeTaken = endTime - startTime;

            JobDetailLog jobDetailLog = JobDetailLog.of(
                jodDetailLogId,
                environment,
                jobResult,
                timeTaken,
                createdAt
            );

            logGoogleSheetsRepository.save(jobDetailLog);

            ExecutionTimeLog executionTimeLog = new ExecutionTimeLog(
                BatchProcessIdContext.getJobId(),
                null,
                logHelper.getEnvironment(),
                BatchProcessType.JOB,
                methodName,
                BatchProcessStatus.COMPLETED,
                "Job 수행 완료",
                jobDetailLog.id(),
                timeTaken,
                logHelper.getCreatedAt(endTime)
            );

            logGoogleSheetsRepository.save(executionTimeLog);

            String executionTimeMessage = notificationService.createExecutionTimeMessage(methodName, timeTaken);

            notificationService.sendMessage(executionTimeMessage);

            return result;
        } catch (Exception exception) {
            long endTime = System.currentTimeMillis();

            long jodDetailLogId = endTime; // 그냥, endTime으로 넣어줄 수 있는데, jodDetailLogId 이 구체적으로 어떤 값이 명시하기 위해 이렇게 함

            long timeTaken = endTime - startTime;

            JobResult jobResult = new JobResult(
                methodName,
                BatchProcessStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(), // TODO : 현재 구조는 어떤 요청에 의해 예외가 발생했는지 모름! -> 개선 필요!
                null,
                null
            );

            JobDetailLog jobDetailLog = JobDetailLog.of(
                jodDetailLogId,
                environment,
                jobResult,
                timeTaken,
                createdAt
            );

            logGoogleSheetsRepository.save(jobDetailLog);

            ExecutionTimeLog executionTimeLog = new ExecutionTimeLog(
                BatchProcessIdContext.getJobId(),
                null,
                logHelper.getEnvironment(),
                BatchProcessType.JOB,
                methodName,
                BatchProcessStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(),
                jobDetailLog.id(),
                timeTaken,
                logHelper.getCreatedAt(endTime)
            );

            logGoogleSheetsRepository.save(executionTimeLog);

            String executionTimeMessage = notificationService.createExecutionTimeMessage(methodName, timeTaken); //

            notificationService.sendMessage(executionTimeMessage); // TODO : 예외도 같이 던져줘야 하나? 아니면 JobId 를 던져줘서 구글 시트로 확인할 수 있게 할까?

            throw exception;
        } finally {
            BatchProcessIdContext.clearJobId();
        }
    }
}

