package com.example.review_study_app.repository.github.aop;


import com.example.review_study_app.service.log.LogService;
import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.service.log.dto.SaveStepLogDto;
import com.example.review_study_app.service.log.LogHelper;
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
public class GithubIssueRestTemplateRepositoryStepLoggingAspect {

    private final LogHelper logHelper;

    private final LogService logService;

    @Autowired
    public GithubIssueRestTemplateRepositoryStepLoggingAspect(
        LogHelper logHelper,
        LogService logService
    ) {
        this.logHelper = logHelper;
        this.logService = logService;
    }

    @Pointcut("target(com.example.review_study_app.repository.github.GithubIssueRestTemplateRepository)")
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
                logHelper.getStepId(),
                logHelper.getJobId(),
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
                logHelper.getStepId(),
                logHelper.getJobId(),
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
