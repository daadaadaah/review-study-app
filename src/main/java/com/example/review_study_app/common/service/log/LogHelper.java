package com.example.review_study_app.common.service.log;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.common.enums.BatchProcessType;
import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import com.example.review_study_app.common.service.log.entity.GithubApiLog;
import com.example.review_study_app.common.service.log.entity.JobDetailLog;
import com.example.review_study_app.common.service.log.entity.StepDetailLog;
import com.example.review_study_app.job.dto.JobResult;
import com.example.review_study_app.task.httpclient.dto.MyHttpRequest;
import com.example.review_study_app.task.httpclient.dto.MyHttpResponse;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

/**
 * LogHelper 는 log에서 자주 사용될만한 함수들을 모아놓은 클래스이다.
 *
 * < UUID로 한 이유>
 * - long 으로 했을 때, id가 겹치는 문제 발생 해서 UUID로 함.
 *
 * < 각 배치 프로세스의 ID 를 ThreadLocal에 저장하는 이유 >
 * - 각 배치 프로세스의 ID가 필요한대, 이걸 각 메서드의 매개변수로 관리하게 되면, 메서드 시그니처가 많아짐
 * - 또한, 한번에 1개의 스케줄러만 동작하므로 단일 스레드 환경이다.
 * - 따라서,ThreadLocal의 값은 스레드에 의해 고유하게 유지되므로, 이 경우 스레드 독립성이 보장되어, 데이터 충돌 우려가 없다.
 * - 따라서, 코드의 간결성과 스레드 독립성이라는 장점을 가지고 있으므로,
 *
 */
@Component
public class LogHelper {

    private final Environment environment;

    private static final ThreadLocal<UUID> threadLocalJobId = new ThreadLocal<>();

    private static final ThreadLocal<UUID> threadLocalStepId = new ThreadLocal<>();

    private static final ThreadLocal<UUID> threadLocalTaskId = new ThreadLocal<>();

    @Autowired
    public LogHelper(
        Environment environment
    ) {
        this.environment =environment;
    }

    /** 공통 **/
    public String getEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();

        String environment = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "";

        return environment;
    }

    public String getCreatedAt(long currentTimeMillis) {
        return getNowInSeoul(currentTimeMillis).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private ZonedDateTime getNowInSeoul(long currentTimeMillis) {
        return Instant.ofEpochMilli(currentTimeMillis).atZone(ZONE_ID_SEOUL);
    }

    /** Job **/
    public void setJobId() {
        UUID uuid = UUID.randomUUID();

        threadLocalJobId.set(uuid);
    }

    public UUID getJobId() {
        return threadLocalJobId.get();
    }

    public void clearJobId() {
        threadLocalJobId.remove();
    }

    public JobDetailLog createJobDetailLog(String methodName, BatchProcessStatus status, String statusReason, JobResult jobResult, long startTime, long endTime) {
        long timeTaken = endTime - startTime;

        long jobDetailLogId = endTime;

        return JobDetailLog.of(
            jobDetailLogId,
            getEnvironment(),
            methodName,
            status,
            statusReason,
            jobResult,
            timeTaken,
            getCreatedAt(startTime)
        );
    }

    public ExecutionTimeLog createJobExecutionTimeLog(String methodName, BatchProcessStatus status, String message, long jobDetailLogId, long startTime, long endTime) {
        long timeTaken = endTime - startTime;

        return ExecutionTimeLog.of(
            getJobId(),
            null,
            getEnvironment(),
            BatchProcessType.JOB,
            methodName,
            status,
            message,
            jobDetailLogId,
            timeTaken,
            getCreatedAt(endTime)
        );
    }

    /** Step **/
    public void setStepId() {
        UUID uuid = UUID.randomUUID();

        threadLocalStepId.set(uuid);
    }

    public UUID getStepId() {
        return threadLocalStepId.get();
    }

    public void clearStepId() {
        threadLocalStepId.remove();
    }

    public <T> StepDetailLog createStepDetailLog(String methodName, BatchProcessStatus status, String statusReason, T result,long startTime, long endTime) {
        long timeTaken = endTime - startTime;

        long stepDetailLogId = endTime;

        return new StepDetailLog(
            stepDetailLogId,
            getEnvironment(),
            methodName,
            status,
            statusReason,
            result,
            timeTaken,
            getCreatedAt(endTime)
        );
    }

    public ExecutionTimeLog createStepExecutionTimeLog(String methodName, BatchProcessStatus status, String message, long stepDetailLogId, long startTime, long endTime) {
        long timeTaken = endTime - startTime;

        return ExecutionTimeLog.of(
            getStepId(),
            getJobId(),
            getEnvironment(),
            BatchProcessType.STEP,
            methodName,
            status,
            message,
            stepDetailLogId,
            timeTaken,
            getCreatedAt(endTime)
        );
    }

    /** Task **/
    public void setTaskId() {
        UUID uuid = UUID.randomUUID();

        threadLocalTaskId.set(uuid);
    }

    public UUID getTaskId() {
        return threadLocalTaskId.get();
    }

    public void clearTaskId() {
        threadLocalTaskId.remove();
    }

    public GithubApiLog createGithubApiLog(String batchProcessName, String httpMethod, MyHttpRequest myHttpRequest, MyHttpResponse myHttpResponse, long startTime, long endTime) {
        long timeTaken = endTime - startTime;

        long taskDetailLogId = endTime;

        String requestBody = ((Object) myHttpRequest.body()) != null ? ((Object) myHttpRequest.body()).toString() : "";

        return new GithubApiLog(
            taskDetailLogId,
            getEnvironment(),
            batchProcessName,
            httpMethod,
            myHttpRequest.url(),
            myHttpRequest.headers(),
            requestBody,
            myHttpResponse.statusCode(),
            myHttpResponse.headers(),
            myHttpResponse.body(),
            timeTaken,
            getCreatedAt(endTime)
        );
    }

    public GithubApiLog createExceptionGithubApiLog(String batchProcessName, String httpMethod, MyHttpRequest myHttpRequest, RestClientResponseException restClientResponseException, long startTime, long endTime) {
        long timeTaken = endTime - startTime;

        long taskDetailLogId = endTime;

        String requestBody = ((Object) myHttpRequest.body()) != null ? ((Object) myHttpRequest.body()).toString() : "";

        return new GithubApiLog(
            taskDetailLogId,
            getEnvironment(),
            batchProcessName,
            httpMethod,
            myHttpRequest.url(),
            myHttpRequest.headers(),
            requestBody,
            restClientResponseException.getStatusCode().value(),
            restClientResponseException.getResponseHeaders(),
            restClientResponseException.getResponseBodyAsString(),
            timeTaken,
            getCreatedAt(endTime)
        );
    }

    public ExecutionTimeLog createTaskExecutionTimeLog(String batchProcessName, BatchProcessStatus status, String message, long taskDetailLogId, long startTime, long endTime ) {
        long timeTaken = endTime - startTime;

        return ExecutionTimeLog.of(
            getTaskId(),
            getStepId(),
            getEnvironment(),
            BatchProcessType.TASK,
            batchProcessName,
            status,
            message,
            taskDetailLogId,
            timeTaken,
            getCreatedAt(endTime)
        );
    }
}
