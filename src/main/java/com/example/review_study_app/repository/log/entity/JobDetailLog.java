package com.example.review_study_app.repository.log.entity;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.service.log.dto.SaveJobLogDto;
import com.example.review_study_app.service.github.vo.GithubApiTaskResult;
import com.example.review_study_app.service.github.dto.GithubJobResult;
import java.util.List;

/**
 * JobDetailLog 는 Job의 상세 로그 데이터를 담는 클래스이다.
 *
 * @param id : 식별자
 * @param environment : 환경변수의 이름
 * @param batchProcessName : 배치 프로세스 이름
 * @param batchProcessStatus : 배치 프로세스 상태
 * @param batchProcessStatusReason : 배치 프로세스 상태 이유
 * @param totalProcessedItemCount : 총 처리된 아이템 수
 * @param successItemCount : 성공한 아이템 수
 * @param successItems : 성공한 아이템 목록(예 : GithubApiTaskResult 의 목록)
 * @param failItemCount : 실패한 아이템 수
 * @param failItems : 실패한 아이템 목록(예 : GithubApiTaskResult 의 목록)
 * @param executionTime : 총 소요시간(ms)
 * @param createdAt : log 생성 시간 (예: 2024-07-21 15:43:10)
 */

public record JobDetailLog(
    long id,
    String environment,
    String batchProcessName,
    BatchProcessStatus batchProcessStatus,
    String batchProcessStatusReason,
    int totalProcessedItemCount,
    int successItemCount,
    List<GithubApiTaskResult> successItems,
    int failItemCount,
    List<GithubApiTaskResult> failItems,
    long executionTime,
    String createdAt
) {
    public static JobDetailLog of(long id, String environment, SaveJobLogDto saveJobLogDto, String createdAt) {

        GithubJobResult githubJobResult = saveJobLogDto.githubJobResult();

        long executionTime = saveJobLogDto.endTime() - saveJobLogDto.startTime();

        return new JobDetailLog(
            id,
            environment,
            saveJobLogDto.methodName(),
            saveJobLogDto.status(),
            saveJobLogDto.statusReason(),
            githubJobResult.successItems().size() + githubJobResult.failItems().size(),
            githubJobResult.successItems().size(),
            githubJobResult.successItems(),
            githubJobResult.failItems().size(),
            githubJobResult.failItems(),
            executionTime,
            createdAt);
    }
}
