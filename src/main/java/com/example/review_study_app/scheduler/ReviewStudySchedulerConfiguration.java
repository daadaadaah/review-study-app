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
//    @Scheduled(cron = "0 10 0 ? * MON", zone = "Asia/Seoul")
    @Scheduled(fixedDelay = 10000)
    public void runCreateNewLabel() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerFacade.createNewWeekNumberLabel(currentYear, currentWeekNumber);
    }

    @Deprecated
    public void runCreateNewIssue() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerFacade.createNewWeeklyReviewIssues(currentYear, currentWeekNumber);
    }

    /**
     * 매주 일요일 PM 11:30 에 이번주 주간회고 이슈를 Close 시키는 스케줄 함수
     */
    @Scheduled(cron = "0 30 23 ? * SUN", zone = "Asia/Seoul")
    public void runCloseIssues() {
        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);

        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);

        reviewStudySchedulerFacade.closeWeeklyReviewIssues(currentYear, currentWeekNumber);
    }

    /**
     * 모든 멤버의 이번주차 주간회고 Issues 를 생성하는 스케줄 함수로, 매주 월요일 AM 00:30 에 동작한다.
     */
//    @Scheduled(cron = "0 30 0 ? * MON", zone = "Asia/Seoul")
//    @Scheduled(fixedDelay = 100000)
//    public void runCreateIssueAsync() {
//        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);
//
//        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);
//
//        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);
//
//        reviewStudySchedulerFacade.createNewWeeklyReviewIssuesAsync(currentYear, currentWeekNumber);
//    }

//    /**
//     * 이번주 주간회고 이슈를 Close 시키는 스케줄 함수로,매주 일요일 PM 11:50 에 동작한다.
//     */
////    @Scheduled(cron = "0 50 23 ? * SUN", zone = "Asia/Seoul")
//    public void runCloseIssuesAsync() {
//        ZonedDateTime seoulDateTime = ZonedDateTime.now(ZONE_ID_SEOUL);
//
//        int currentYear = MyDateUtils.getCurrentYear(seoulDateTime);
//
//        int currentWeekNumber = MyDateUtils.getCurrentWeekNumber(seoulDateTime);
//
//        reviewStudySchedulerFacade.closeWeeklyReviewIssuesAsync(currentYear, currentWeekNumber);
//    }
}
