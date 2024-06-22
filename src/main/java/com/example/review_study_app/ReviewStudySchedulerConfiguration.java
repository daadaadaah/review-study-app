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

    private final GithubIssueService githubService;

    private final NotificationService notificationService;

    @Autowired
    public ReviewStudySchedulerConfiguration(
        GithubIssueService githubService,
        NotificationService notificationService
    ) {
        this.githubService = githubService;
        this.notificationService = notificationService;
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

            String newLabelCreationSuccessMessage = notificationService.createNewLabelCreationSuccessMessage(weekNumberLabelName);

            notificationService.sendMessage(newLabelCreationSuccessMessage);
        } catch (Exception exception) {
            log.error("새로운 라벨 생성이 실패했습니다. exception = {} labelName= {} ", exception.getMessage(), weekNumberLabelName);

            String newLabelCreationFailureMessage = notificationService.createNewLabelCreationFailureMessage(weekNumberLabelName, exception);

            notificationService.sendMessage(newLabelCreationFailureMessage);
        }
    }

    @Scheduled(initialDelay = 3000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCreateNewIssue() {
        // 1. 날짜 계산
        int currentYear = ReviewStudyDateUtils.getCurrentYear();

        int currentWeekNumber = ReviewStudyDateUtils.getCurrentWeekNumber();

        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        if(!githubService.isWeekNumberLabelPresent(weekNumberLabelName)) {
            runCreateNewLabel();
        }

        // 2. 이슈 생성
        List<GithubApiSuccessResult> githubApiSuccessResults = new ArrayList<>();

        List<GithubApiFailureResult> githubApiFailureResults = new ArrayList<>();

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

                GithubApiSuccessResult githubApiSuccessResult = new GithubApiSuccessResult(newGhIssue.getNumber(), newGhIssue.getTitle());

                githubApiSuccessResults.add(githubApiSuccessResult);

            } catch (Exception exception) {
                log.error("새로운 이슈 생성이 실패했습니다. issueTitle = {}, exception = {} ", issueTitle, exception.getMessage());

                GithubApiFailureResult githubApiFailureResult = new GithubApiFailureResult(null, issueTitle, exception.getMessage());

                githubApiFailureResults.add(githubApiFailureResult);
            }
        });

        // 3. Discord 로 Github 통신 결과 보내기
        // (1) 성공 결과 모음
        String successResult = githubApiSuccessResults.isEmpty()
            ? ""
            : githubApiSuccessResults.stream()
                .map(result -> notificationService.createNewIssueCreationSuccessMessage(weekNumberLabelName, result))
                .collect(Collectors.joining("\n"));


        // (2) 실패 결과 모음
        String failureResult = githubApiFailureResults.isEmpty()
            ? ""
            : githubApiFailureResults.stream()
                .map(result -> notificationService.createNewIssueCreationFailureMessage(weekNumberLabelName, result))
                .collect(Collectors.joining("\n"));

        // (3) 최종 결과
        String finalResult = successResult+"\n\n"+failureResult;

        // (4) Discord 로 보내기
        notificationService.sendMessage(finalResult);
    }

    @Scheduled(initialDelay = 6000) // TODO : 테스트용, 실제론 cron 활용할 꺼임
    public void runCloseIssues() {
        // 1. 날짜 계산
        int currentYear = ReviewStudyDateUtils.getCurrentYear();

        int currentWeekNumber = ReviewStudyDateUtils.getCurrentWeekNumber();

        String labelNameToClose = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        // 2. 이슈 Close
        List<GHIssue> closedIssues = new ArrayList<>();

        List<GithubApiSuccessResult> githubApiSuccessResults = new ArrayList<>();

        List<GithubApiFailureResult> githubApiFailureResults = new ArrayList<>();

        try {
            closedIssues = githubService.getIssuesToClose(labelNameToClose);

            log.info("Close 할 이슈 목록 가져오기 성공했습니다. labelNameToClose = {} ", labelNameToClose);

        } catch (Exception exception) {
            log.error("Close 할 이슈 목록 가져오는 것을 실패했습니다. exception = {}", exception.getMessage());

            String issueFetchFailureMessage = notificationService.createIssueFetchFailureMessage(labelNameToClose,exception);

            notificationService.sendMessage(issueFetchFailureMessage);
            return;
        }

        if(closedIssues.isEmpty()) {
            log.info("Close 할 이슈가 없습니다. ");

            String emptyIssuesToCloseMessage = notificationService.createEmptyIssuesToCloseMessage(labelNameToClose);

            notificationService.sendMessage(emptyIssuesToCloseMessage);

            return;
        }

        log.info("("+labelNameToClose+") 주간회고 이슈 Close 시작");

        closedIssues.stream().forEach(ghIssue -> {
            int issueNumber = ghIssue.getNumber();

            String issueTitle = ghIssue.getTitle();

            try {

                githubService.closeIssue(issueNumber);

                log.info("이슈가 Close 되었습니다. issueTitle = {}, issueNumber = {} ", issueTitle, issueNumber);

                GithubApiSuccessResult githubApiSuccessResult = new GithubApiSuccessResult(issueNumber, issueTitle);

                githubApiSuccessResults.add(githubApiSuccessResult);

            } catch (Exception exception) {
                log.error("이슈 Close에 실패했습니다. issueTitle = {}, issueNumber = {}, exception = {} ", issueTitle, issueNumber, exception.getMessage());

                GithubApiFailureResult githubApiFailureResult = new GithubApiFailureResult(issueNumber, issueTitle, exception.getMessage());

                githubApiFailureResults.add(githubApiFailureResult);
            }
        });

        log.info("("+labelNameToClose+") 주간회고 이슈 Close 완료");

        // 3. Discord 로 Github 통신 결과 보내기
        // (1) 성공 결과 모음
        String successResult = githubApiSuccessResults.isEmpty()
            ? ""
            : githubApiSuccessResults.stream()
                .map(result -> notificationService.createIssueCloseSuccessMessage(labelNameToClose, result))
                .collect(Collectors.joining("\n"));


        // (2) 실패 결과 모음
        String failureResult = githubApiFailureResults.isEmpty()
            ? ""
            : githubApiFailureResults.stream()
                .map(result -> notificationService.createIssueCloseFailureMessage(labelNameToClose, result))
                .collect(Collectors.joining("\n"));

        // (3) 최종 결과
        String finalResult = successResult+"\n\n"+failureResult;

        // (4) Discord 로 보내기
        notificationService.sendMessage(finalResult);
    }
}
