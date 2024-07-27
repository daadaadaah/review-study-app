package com.example.review_study_app.log;

import com.example.review_study_app.github.BatchProcessStatus;

/**
 *
 * ExecutionTimeLog 는 모든 Job, Step, Task의 실행 시간을 파악하기 위해 필요한 데이터를 담는 클래스이다.
 *
 * @param id : 식별자
 * @param parentId : 부모 식별자 (예 : Step의 경우 jobId, Task의 경우 taskId)
 * @param environment : 개발환경 (예 : local)
 * @param batchProcessType : 배치 프로세스 유형 (예 : JOB, STEP, TASK)
 * @param batchProcessName : 배치 프로세스 이름 (예 : 각 배치 프로세스 클래스의 메서드 이름)
 * @param batchProcessStatus : 배치 프로세스 상태 (예 : 완료/중지)
 * @param batchProcessStatusReason : 배치 프로세스 상태 이유 (예 : 성공/예외 발생)
 * @param detailLogId : 각 배치 프로세스의 상세 로그 Id -> 배치 결과는 상세 로그 가서 확인하도록!
 * @param executionTime : 배치 프로세스 실행 시간
 * @param createdAt : log 생성 시간 (예 : 2024-07-21 15:43:10)
 */
public record ExecutionTimeLog<T>(
    String id,
    String parentId,
    String environment,
    BatchProcessType batchProcessType,
    String batchProcessName,
    BatchProcessStatus batchProcessStatus,
    String batchProcessStatusReason,
    Long detailLogId,
    long executionTime,
    String createdAt
) {

}
