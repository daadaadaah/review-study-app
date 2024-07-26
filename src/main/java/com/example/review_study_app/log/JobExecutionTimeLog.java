package com.example.review_study_app.log;

import com.example.review_study_app.github.GithubApiTaskResult;
import com.example.review_study_app.github.JobResult;
import com.example.review_study_app.github.JobStatus;
import java.util.List;

/**
 * JobExecutionTimeLog 는 작업별로 소요시간을 로깅하기 위한 데이터를 담는 클래스이다.
 *
 * @param id : 식별자(createdAt의 long 값)
 * @param environment : 환경변수의 이름
 * @param jobType : Job 유형(: GithubFacade의 메서드들)
 * @param jobStatus : Job 상태
 * @param jobStatusReason : Job 상태 이유
 * @param totalTaskCount : 총 task 갯수
 * @param successTaskCount : 성공 task 갯수
 * @param successTasks : 성공 task 목록
 * @param failTaskCount : 실패 task 갯수
 * @param failTasks : 실패 task 목록
 * @param executionTime : Job의 총 소요시간
 * @param createdAt : log가 만들어진 시간(예 : 2024-07-21 15:43:10)
 */
public record JobExecutionTimeLog(
    long id,

    String environment,

    String jobType,

    JobStatus jobStatus,

    String jobStatusReason,

    int totalTaskCount,

    int successTaskCount,

    List<GithubApiTaskResult> successTasks,

    int failTaskCount,

    List<GithubApiTaskResult> failTasks,

    long executionTime,

    String createdAt
) {

    public static JobExecutionTimeLog of(long id, String environment, JobResult jobResult, long executionTime, String createdAt) {
        return new JobExecutionTimeLog(
            id,
            environment,
            jobResult.jobType(),
            jobResult.jobStatus(),
            jobResult.jobStatusReason(),
            jobResult.totalTaskCount(),
            jobResult.successTaskCount(),
            jobResult.successTasks(),
            jobResult.failTaskCount(),
            jobResult.failTasks(),
            executionTime,
            createdAt);
    }
}
