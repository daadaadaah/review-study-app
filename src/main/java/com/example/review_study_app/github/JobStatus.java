package com.example.review_study_app.github;

/**
 * JobStatus 는 Job의 상태를 나타내는 클래스이다.
 */
public enum JobStatus {
    COMPLETED, // Github 통신이 성공이든 실패든 Job 실행이 완료된 상태
    STOPPED; // Github 통신 실패 등 다양한 이유로 Job 실행이 중간에 중단된 상태
}
