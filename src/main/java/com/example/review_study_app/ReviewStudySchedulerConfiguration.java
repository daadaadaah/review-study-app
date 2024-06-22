package com.example.review_study_app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewStudySchedulerConfiguration {

    private final ReviewStudySchedulerFacade reviewStudySchedulerFacade;

    @Autowired
    public ReviewStudySchedulerConfiguration(
        ReviewStudySchedulerFacade reviewStudySchedulerFacade
    ) {
        this.reviewStudySchedulerFacade = reviewStudySchedulerFacade;
    }

    /**
     * 매주 월요일 AM 00:10 에 이번주차 Label 을 생성하는 스케줄 함수
     */
    @Scheduled(cron = "0 10 0 ? * MON", zone = "Asia/Seoul")
    public void runCreateNewLabel() {
        reviewStudySchedulerFacade.createNewWeekNumberLabel();
    }

    /**
     * 매주 월요일 AM 00:30 에 모든 멤버의 이번주차 주간회고 Issues 를 생성하는 스케줄 함수
     */
    @Scheduled(cron = "0 30 0 ? * MON", zone = "Asia/Seoul")
    public void runCreateNewIssue() {
        reviewStudySchedulerFacade.createNewWeeklyReviewIssues();
    }

    /**
     * 매주 일요일 PM 11:30 에 이번주 주간회고 이슈를 Close 시키는 스케줄 함수
     */
    @Scheduled(cron = "0 30 23 ? * SUN", zone = "Asia/Seoul")
    public void runCloseIssues() {
        reviewStudySchedulerFacade.closeWeeklyReviewIssues();
    }
}
