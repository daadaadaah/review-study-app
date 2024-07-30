package com.example.review_study_app.scheduler;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.utils.MyDateUtils;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewStudyScheduler {

    private final ReviewStudySchedulerFacade reviewStudySchedulerFacade;

    @Autowired
    public ReviewStudyScheduler(
        ReviewStudySchedulerFacade reviewStudySchedulerFacade
    ) {
        this.reviewStudySchedulerFacade = reviewStudySchedulerFacade;
    }

    /**
     * 매주 월요일 AM 00:10 에 이번주차 Label 을 생성하는 스케줄 함수
     */
    @Scheduled(cron = "0 10 0 ? * MON", zone = "Asia/Seoul")
    public void runCreateNewLabel() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerFacade.createNewWeekNumberLabel(currentYear, currentWeekNumber);
    }

    /**
     * 매주 월요일 AM 00:30 에 모든 멤버의 이번주차 주간회고 Issues 를 생성하는 스케줄 함수
     */
//    @Scheduled(cron = "0 30 0 ? * MON", zone = "Asia/Seoul")
    @Scheduled(fixedRate = 1200000) // TODO : 테스트용
    public void runCreateNewIssue() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerFacade.createNewWeeklyReviewIssues(currentYear, currentWeekNumber);
    }

    /**
     * 매주 일요일 PM 11:50 에 이번주 주간회고 이슈를 Close 시키는 스케줄 함수
     */
    @Scheduled(cron = "0 50 23 ? * SUN", zone = "Asia/Seoul")
    public void runCloseIssues() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerFacade.closeWeeklyReviewIssues(currentYear, currentWeekNumber);
    }
}
