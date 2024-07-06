package com.example.review_study_app.scheduler;


import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.utils.MyDateUtils;
import com.example.review_study_app.github.GithubApiFailureResult;
import com.example.review_study_app.github.GithubApiSuccessResult;
import com.example.review_study_app.github.GithubIssueService;
import com.example.review_study_app.github.GithubIssueWebclientService;
import com.example.review_study_app.notification.NotificationService;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReviewStudySchedulerFacade {

    private final GithubIssueService githubIssueService;

    private final GithubIssueWebclientService githubIssueWebclientService;

    private final NotificationService notificationService;

    @Autowired
    public ReviewStudySchedulerFacade(
        GithubIssueService githubIssueService,
        GithubIssueWebclientService githubIssueWebclientService,
        NotificationService notificationService
    ) {
        this.githubIssueService = githubIssueService;
        this.githubIssueWebclientService = githubIssueWebclientService;
        this.notificationService = notificationService;
    }

    /**
     * 새로운 주차 Label을 생성하는 함수 - 라벨 중복 체크 로직 추가 안 이유 : 이름이 중복되면 라벨 자체가 생성이 안 되므로, 이떄, 다음과 같은 에러 메시지가
     * 나온다. 에러 메시지 : {"message":"Validation
     * Failed","errors":[{"resource":"Label","code":"already_exists","field":"name"}],"documentation_url":"https://docs.github.com/rest/issues/labels#create-a-label"}
     */
    public void createNewWeekNumberLabel(int year, int weekNumber) {
        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        log.info("새로운 라벨 생성을 시작합니다. labelName = {} ", weekNumberLabelName);

        try {
            githubIssueWebclientService.createNewLabel(year, weekNumber);

            log.info("새로운 라벨 생성이 성공했습니다. labelName = {} ", weekNumberLabelName);

            String newLabelCreationSuccessMessage = notificationService.createNewLabelCreationSuccessMessage(
                weekNumberLabelName);

            notificationService.sendMessage(newLabelCreationSuccessMessage);
        } catch (Exception exception) {
            log.error("새로운 라벨 생성이 실패했습니다. exception = {} labelName= {} ", exception.getMessage(),
                weekNumberLabelName);

            String newLabelCreationFailureMessage = notificationService.createNewLabelCreationFailureMessage(
                weekNumberLabelName, exception);

            notificationService.sendMessage(newLabelCreationFailureMessage);
        }
    }


    /**
     * 이번주 주간회고 여러개 Issue를 생성하는 함수
     */
    public void createNewWeeklyReviewIssues(int year, int weekNumber) {
        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        if(!githubIssueService.isWeekNumberLabelPresent(weekNumberLabelName)) {
            createNewWeekNumberLabel(year, weekNumber);
        }

        // 1. 이슈 생성
        List<GithubApiSuccessResult> githubApiSuccessResults = new ArrayList<>();

        List<GithubApiFailureResult> githubApiFailureResults = new ArrayList<>();

        ReviewStudyInfo.MEMBERS.stream().forEach(member -> {
            String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(year, weekNumber, member.fullName()); // TODO : 서비스에서만 도메인 객체 알도록 변경 필요

            try {

                GHIssue newGhIssue = githubIssueService.createNewIssue(
                    year,
                    weekNumber,
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

        // 2. Discord 로 Github 통신 결과 보내기
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

    /**
     * 이번주의 모든 주간회고 Issue를 Close 하는 함수
     */
    public void closeWeeklyReviewIssues(int year, int weekNumber) {
        String labelNameToClose = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        // 1. 이슈 Close
        List<GHIssue> closedIssues = new ArrayList<>();

        List<GithubApiSuccessResult> githubApiSuccessResults = new ArrayList<>();

        List<GithubApiFailureResult> githubApiFailureResults = new ArrayList<>();

        try {
            closedIssues = githubIssueService.getIssuesToClose(labelNameToClose);

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

                githubIssueService.closeIssue(issueNumber);

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

        // 2. Discord 로 Github 통신 결과 보내기
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