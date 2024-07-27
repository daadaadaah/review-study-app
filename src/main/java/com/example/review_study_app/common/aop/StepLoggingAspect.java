package com.example.review_study_app.common.aop;


import com.example.review_study_app.common.utils.BatchProcessIdContext;
import com.example.review_study_app.github.BatchProcessStatus;
import com.example.review_study_app.log.StepDetailLog;
import com.example.review_study_app.log.BatchProcessType;
import com.example.review_study_app.log.ExecutionTimeLog;
import com.example.review_study_app.log.LogGoogleSheetsRepository;
import com.example.review_study_app.log.LogHelper;
import com.example.review_study_app.notification.NotificationService;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class StepLoggingAspect {

    private final LogHelper logHelper;

    private final NotificationService notificationService;

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    @Autowired
    public StepLoggingAspect(
        LogHelper logHelper,
        NotificationService notificationService,
        LogGoogleSheetsRepository logGoogleSheetsRepository
    ) {
        this.logHelper = logHelper;
        this.notificationService = notificationService;
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
    }

    @Pointcut("target(com.example.review_study_app.github.GithubIssueService)")
    public void targetImplementingInterface() {}

    @Around("targetImplementingInterface()") // TODO : StepLogging 어노테이션 붙은 클래스의 모든 메서드에서 동작되도록 수정하기
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        UUID uuid = UUID.randomUUID();

        BatchProcessIdContext.setStepId(uuid);

        String environment = logHelper.getEnvironment();

        String methodName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            String stepId = BatchProcessIdContext.getStepId();

            String parentId = BatchProcessIdContext.getJobId();

            long endTime = System.currentTimeMillis();

            String createdAt = logHelper.getCreatedAt(endTime);

            long timeTaken = endTime - startTime;

            long stepDetailLogId = endTime;

            StepDetailLog stepDetailLog = new StepDetailLog(
                stepDetailLogId,
                environment,
                methodName,
                BatchProcessStatus.COMPLETED,
                "Step 수행 완료",
                result,
                createdAt
            );

            logGoogleSheetsRepository.save(stepDetailLog);

            ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
                stepId,
                parentId,
                environment,
                BatchProcessType.STEP,
                methodName,
                BatchProcessStatus.COMPLETED,
                "Step 수행 완료",
                endTime,
                timeTaken,
                createdAt
            );

            logGoogleSheetsRepository.save(executionTimeLog);

            return result;
        } catch (Exception exception) {
            long endTime = System.currentTimeMillis();

            String stepId = BatchProcessIdContext.getStepId();

            String parentId = BatchProcessIdContext.getJobId();

            String createdAt = logHelper.getCreatedAt(endTime);

            long timeTaken = System.currentTimeMillis() - startTime;

            long stepDetailLogId = endTime;

            StepDetailLog stepDetailLog = new StepDetailLog(
                stepDetailLogId,
                environment,
                methodName,
                BatchProcessStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(),
                exception, // TODO : 어떤걸 넣어주는게 좋을까?
                createdAt
            );

            logGoogleSheetsRepository.save(stepDetailLog);

            ExecutionTimeLog executionTimeLog = ExecutionTimeLog.of(
                stepId,
                parentId,
                environment,
                BatchProcessType.STEP,
                methodName,
                BatchProcessStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(),
                endTime,
                timeTaken,
                createdAt
            );

            logGoogleSheetsRepository.save(executionTimeLog);

            throw exception;
        } finally {
            BatchProcessIdContext.clearStepId();
        }
    }
}
