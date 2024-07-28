package com.example.review_study_app.job.dto;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.job.dto.GithubApiTaskResult;
import java.util.List;

/**
 * JobResult 은 Job 수행 결과를 저장하는 클래스이다.
 * @param batchProcessName : GithubJobFacade 의 각 메서드들(예 : batchCreateNewWeeklyReviewIssues)
 * @param batchProcessStatus : Job 상태
 * @param batchProcessStatusReason : Job 상태 이유
 * @param successItems : 성공 Item 목록 (예 : GithubApiTaskResult 목록)
 * @param failItems : 실패 Item 목록 (예 : GithubApiTaskResult 목록)
 */
public record JobResult( // TODO : 용어 수정 필요!
    String batchProcessName,

    BatchProcessStatus batchProcessStatus,

    String batchProcessStatusReason,

    List<GithubApiTaskResult> successItems,

    List<GithubApiTaskResult> failItems
) {

}
