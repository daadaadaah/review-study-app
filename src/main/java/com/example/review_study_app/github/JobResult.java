package com.example.review_study_app.github;

import java.util.List;

/**
 * JobResult 은 Job 수행 결과를 저장하는 클래스이다.
 * @param jobType : GithubJobFacade 의 각 메서드들(예 : batchCreateNewWeeklyReviewIssues)
 * @param jobStatus : Job 상태
 * @param jobStatusReason : Job 상태의 이유
 * @param totalTaskCount : 총 Task 갯수
 * @param successTaskCount : 성공 Task 갯수
 * @param successTaskIds : 성공 TaskId(GithubApiLog의 식별자) 리스트
 * @param failTaskCount : 실패 Task 갯수
 * @param failTaskIds : 실패 TaskId(GithubApiLog의 식별자) 리스트
 * @param executionTime : Job 총 실행 시간
 */
public record JobResult(
    long jobId,

    String jobType,

    JobStatus jobStatus,

    String jobStatusReason,

    int totalTaskCount,

    int successTaskCount,

    List<GithubApiTaskResult> successTaskIds,

    int failTaskCount,

    List<GithubApiTaskResult> failTaskIds,

    long executionTime
) {

}
