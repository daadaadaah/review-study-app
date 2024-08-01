package com.example.review_study_app.step.aop;


import com.example.review_study_app.common.service.log.LogService;
import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.common.service.log.dto.SaveStepLogDto;
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
@Order(value = 2)
public class GithubIssueServiceStepLoggingAspect {

    private final LogHelper logHelper;

    private final LogService logService;

    @Autowired
    public GithubIssueServiceStepLoggingAspect(
        LogHelper logHelper,
        LogService logService
    ) {
        this.logHelper = logHelper;
        this.logService = logService;
    }

    @Pointcut("target(com.example.review_study_app.step.GithubIssueServiceStep)")
    public void targetImplementingInterface() {}

    /**
     * 이렇게 구현한 이유는 `GithubJobFacadeLoggingAspect`와 비슷한 이유이므로, `GithubJobFacadeLoggingAspect` 참고!
     */
    @Around("targetImplementingInterface()")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            long endTime = System.currentTimeMillis();

            saveStepLog(new SaveStepLogDto(
                methodName,
                BatchProcessStatus.COMPLETED,
                "Step 수행 완료",
                result,
                startTime,
                endTime
            ));

            return result;
        } catch (Exception exception) {
            long endTime = System.currentTimeMillis();


            saveStepLog(new SaveStepLogDto(
                methodName,
                BatchProcessStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(),
                exception,
                startTime,
                endTime
            ));

            throw exception;
        }
    }

    private void saveStepLog(SaveStepLogDto saveStepLogDto) {
        logService.saveStepLog(saveStepLogDto);
    }
}
