package com.example.review_study_app;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssue;
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

    @Scheduled(initialDelay = 1000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCreateNewLabel() {
        reviewStudySchedulerFacade.createNewWeekNumberLabel();
    }

    @Scheduled(initialDelay = 3000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCreateNewIssue() {
        reviewStudySchedulerFacade.createNewWeeklyReviewIssues();
    }

    @Scheduled(initialDelay = 6000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCloseIssues() {
        reviewStudySchedulerFacade.closeWeeklyReviewIssues();
    }
}
