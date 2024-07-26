package com.example.review_study_app.github;

/**
 * BatchProcessStatus 는 Job의 상태를 나타내는 클래스이다.
 */
public enum BatchProcessStatus {
    COMPLETED, // Github 통신이 성공이든 실패든 Job 실행이 완료된 상태 // TODO : 이거 부분 실패의 경우, STOP 으로 할지 정해야 됨!
    STOPPED; // Github 통신 실패 등 다양한 이유로 Job 실행이 중간에 중단된 상태
}
