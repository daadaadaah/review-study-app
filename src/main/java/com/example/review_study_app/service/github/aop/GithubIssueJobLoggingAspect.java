package com.example.review_study_app.service.github.aop;


import com.example.review_study_app.service.log.LogService;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.github.dto.GithubJobResult;
import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.service.log.helper.LogHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * GithubJobFacadeLoggingAspect 는 GihtubFacade의 JobResult와 GihtubFacade의 수행시간을 로깅하기 위한 AOP 클래스 입니다.
 */

@Slf4j
@Aspect
@Component
@Order(value = 2)
public class GithubIssueJobLoggingAspect { // TODO : 이름 GithubJobLoggingAspect 로 수정 필요!

    private final LogHelper logHelper;

    private final LogService logService;

    @Autowired
    public GithubIssueJobLoggingAspect(
        LogHelper logHelper,
        LogService logService
    ) {
        this.logHelper = logHelper;
        this.logService = logService;
    }

    /**
     * < 유연한 AOP 적용 기준으로 하지 않은 이유>
     * - 기능이 확장되더라도, GithubJobFacade 내의 메서드 추가 정도만 될 것 같아서, 유연함 보다 단순한 구현으로 택했다.
     *
     * - Job 로깅 AOP 구현 방법으로 2가지를 생각했다.
     *      방법 1. (현재 방법) GithubJobFacade 라고 구체적인 클래스
     *      방법 2. @JobLogging 하나 만들어서 @JobLogging 이 붙은 클래스에 AOP를 적용하는 방법
     *
     * - 방법 2의 경우, 방법 1 보다 확장성 있는 구조라 유연하다는 장점이 있지만,
     * - 새로운 Job 유형의 JobResult 가 지금과 다른 구조를 지닐 수 있어서, 섣부른 추상화보다 단순한 구현이 나을 것 같아서 방법 1를 선택했다.
     * - 만약, 추후에 기능이 새로운 유형의 Job 이 생긴다면, 그때 가서 수정해 보는게 좋다고 생각했다.
     * - 만약, 유연함이 필요하다면, 다음과 같은 방법들을 그때 적용해 봐도 좋을 것 같다.
     *      방법 1. 위의 방법 2
     *      방법 2. 클래스명의 Job이 들어가면, AOP가 적용되는 방법
     *      방법 3. job 이라는 패키지에 Job들을 모아놓고, job이라는 패키지들에게 AOP를 적용하는 방법
     *
     */
    @Around("execution(* com.example.review_study_app.service.github.GithubIssueJobService.*(..))")
    public Object logAroundMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();

        try {

            Object result = joinPoint.proceed(); // 함수 실행

            if(result instanceof GithubJobResult) {
                GithubJobResult githubJobResult = (GithubJobResult) result; // TODO : 클로즈할 이슈가 없을 때 예외남.

                long endTime = System.currentTimeMillis();

                saveJobLog(new SaveJobLogDto(
                    logHelper.getJobId(),
                    methodName,
                    BatchProcessStatus.COMPLETED,
                    "Job 수행 성공",
                    githubJobResult,
                    startTime,
                    endTime
                ));

                return result;
            } else {
                // TODO : 임시로 예외 던져주기
                throw new RuntimeException("GithubFacade 의 return 값은 JobResult 이어야 합니다. 해당 메소드를 확인해주세요. methodName="+ methodName);

            }
        } catch (Exception exception) {
            long endTime = System.currentTimeMillis();

            saveJobLog(new SaveJobLogDto(
                logHelper.getJobId(),
                methodName,
                BatchProcessStatus.STOPPED,
                "예외 발생 : "+exception.getMessage(),
                null,
                startTime,
                endTime
            ));

            throw exception;
        }
    }

    private void saveJobLog(SaveJobLogDto saveJobLogDto) {
//        logService.saveJobLog(saveJobLogDto);
    }
}

