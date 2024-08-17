package com.example.review_study_app.service.github.dto;

import com.example.review_study_app.service.github.vo.GithubApiTaskResult;
import java.util.List;

/**
 * JobResult 은 Job 수행 결과를 저장하는 클래스이다.
 * @param successItems : 성공 Item 목록 (예 : GithubApiTaskResult 목록)
 * @param failItems : 실패 Item 목록 (예 : GithubApiTaskResult 목록)
 */
public record GithubJobResult(

    List<GithubApiTaskResult> successItems,

    List<GithubApiTaskResult> failItems
) {

}
