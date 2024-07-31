package com.example.review_study_app.common.service.log.entity;

import com.example.review_study_app.common.enums.BatchProcessStatus;

/**
 * StepDetailLog 는 배치 프로세스 중 Step 의 상세 로그를 담는 클레스이다.
 *
 * @param id : 식별자
 * @param environment : 환경변수의 이름 (예 : local)
 * @param batchProcessName : 배치 프로세스 이름
 * @param batchProcessStatus : 배치 프로세스 상태
 * @param batchProcessStatusReason : 배치 프로세스 상태 이유
 * @param stepReturnValue : Step 의 return 값
 * @param executionTime : 소요시간
 * @param createdAt : log 생성 시간 (예 :  2024-07-21 15:43:10)
 */

public record StepDetailLog<T>(
    long id,
    String environment,
    String batchProcessName,
    BatchProcessStatus batchProcessStatus,
    String batchProcessStatusReason,
    T stepReturnValue,
    long executionTime,
    String createdAt
) {

}
