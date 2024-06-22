package com.example.review_study_app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewStudySchedulerConfiguration {

    private final GithubIssueService githubService;

    @Autowired
    public ReviewStudySchedulerConfiguration(
        GithubIssueService githubService
    ) {
        this.githubService = githubService;
    }

    @Scheduled(initialDelay = 1000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCreateNewLabel() {

        int currentYear = ReviewStudyDateUtils.getCurrentYear();

        int currentWeekNumber = ReviewStudyDateUtils.getCurrentWeekNumber();

        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        log.info("새로운 라벨 생성을 시작합니다. labelName = {} ", weekNumberLabelName);

        try {
            githubService.createNewLabel(currentYear, currentWeekNumber);

            log.info("새로운 라벨 생성이 성공했습니다. labelName = {} ", weekNumberLabelName);

        } catch (Exception exception) {
            log.error("새로운 라벨 생성이 실패했습니다. exception = {} labelName= {} ", exception.getMessage(), weekNumberLabelName);
        }
    }
}
