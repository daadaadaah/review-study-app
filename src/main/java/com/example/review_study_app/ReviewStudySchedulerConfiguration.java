package com.example.review_study_app;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssue;
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

//    @Scheduled(initialDelay = 1000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
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

//    @Scheduled(initialDelay = 1000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCreateNewIssue() {

        int currentYear = ReviewStudyDateUtils.getCurrentYear();

        int currentWeekNumber = ReviewStudyDateUtils.getCurrentWeekNumber();

        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        if(!githubService.isWeekNumberLabelPresent(weekNumberLabelName)) {
            try {
                log.info("새로운 라벨 생성을 시작합니다. labelName = {} ", weekNumberLabelName);

                githubService.createNewLabel(currentYear, currentWeekNumber);

                log.info("새로운 라벨 생성이 성공했습니다. labelName = {} ", weekNumberLabelName);

            } catch (Exception exception) {
                log.error("새로운 라벨 생성이 실패했습니다. exception = {} labelName = {}", exception.getMessage(), weekNumberLabelName);
                return;
            }

        }

        ReviewStudyInfo.MEMBERS.stream().forEach(member -> {
            String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(currentYear, currentWeekNumber, member.fullName()); // TODO : 서비스에서만 도메인 객체 알도록 변경 필요

            try {

                GHIssue newGhIssue = githubService.createNewIssue(
                    currentYear,
                    currentWeekNumber,
                    member.fullName(),
                    member.githubName()
                );

                log.info("새로운 이슈가 생성되었습니다. issueTitle = {}, issueNumber = {} ", issueTitle, newGhIssue.getNumber());

            } catch (Exception exception) {
                log.error("새로운 이슈 생성이 실패했습니다. issueTitle = {}, exception = {} ", issueTitle, exception.getMessage());
            }
        });
    }

    @Scheduled(initialDelay = 1000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCloseIssues() {
        int currentYear = ReviewStudyDateUtils.getCurrentYear();

        int currentWeekNumber = ReviewStudyDateUtils.getCurrentWeekNumber();

        String labelNameToClose = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        List<GHIssue> closedIssues = new ArrayList<>();

        try {
            closedIssues = githubService.getIssuesToClose(labelNameToClose);

        } catch (Exception exception) {
            log.error("Close 할 이슈 목록 가져오는 것을 실패했습니다. exception = {}", exception.getMessage());
        }

        if(closedIssues.isEmpty()) {
            log.info("Close 할 이슈가 없습니다. ");
            return;
        }

        log.info("("+labelNameToClose+") 주간회고 이슈 Close 시작");

        closedIssues.stream().forEach(ghIssue -> {
            int issueNumber = ghIssue.getNumber();

            String issueTitle = ghIssue.getTitle();

            try {

                githubService.closeIssue(issueNumber);

                log.info("이슈가 Close 되었습니다. issueTitle = {}, issueNumber = {} ", issueTitle, issueNumber);
            } catch (Exception exception) {
                log.error("이슈 Close에 실패했습니다. issueTitle = {}, issueNumber = {}, exception = {} ", issueTitle, issueNumber, exception.getMessage());

            }
        });

        log.info("("+labelNameToClose+") 주간회고 이슈 Close 완료");
    }
}
