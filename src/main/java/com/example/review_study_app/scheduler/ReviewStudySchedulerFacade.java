package com.example.review_study_app.scheduler;



import com.example.review_study_app.service.github.dto.GithubJobResult;
import com.example.review_study_app.service.github.exception.GetIssuesToCloseFailException;
import com.example.review_study_app.service.github.exception.IsWeekNumberLabelPresentFailException;
import com.example.review_study_app.service.github.exception.IssuesToCloseIsEmptyException;
import com.example.review_study_app.service.github.domain.GithubIssueApiFailureResult;
import com.example.review_study_app.service.github.domain.GithubIssueApiSuccessResult;
import com.example.review_study_app.service.github.GithubIssueJobService;
import com.example.review_study_app.service.notification.NotificationService;
import com.example.review_study_app.domain.ReviewStudyInfo;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * ReviewStudySchedulerFacade 는 Github Job 작업을 실행하고, 그 결과를 Notification 으로 전달하는 책임을 담당하는 클래스이다.
 */
@Slf4j
@Component
public class ReviewStudySchedulerFacade {

    private final GithubIssueJobService githubIssueJobService;

    private final NotificationService notificationService;

    @Autowired
    public ReviewStudySchedulerFacade(
        GithubIssueJobService githubIssueJobService,
        NotificationService notificationService
    ) {
        this.githubIssueJobService = githubIssueJobService;
        this.notificationService = notificationService;
    }

    /**
     * createNewWeekNumberLabel 는 새로운 주차 Label을 생성하는 메서드이다.
     *
     * < 라벨 중복 체크 로직 추가 안 이유 >
     * - Github 라벨 생성 API 자체가 멱등성이 보장되므로,
     * - 만약, 중복된 라벨을 생성하려고 하면, 다음과 같은 에러 메시지가 나온다.
     * 에러 메시지 : {"message":"Validation Failed","errors":[{"resource":"Label","code":"already_exists","field":"name"}],"documentation_url":"https://docs.github.com/rest/issues/labels#create-a-label"}
     *
     */
    public void createNewWeekNumberLabel(int year, int weekNumber) {
        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        log.info("주차 라벨 생성을 시작합니다. labelName = {} ", weekNumberLabelName);

        try {
            githubIssueJobService.createNewLabelJob(year, weekNumber);

            log.info("주차 라벨 생성이 성공했습니다. labelName = {} ", weekNumberLabelName);

            notifyCreateNewWeekNumberLabelSuccessResult(weekNumberLabelName);
        } catch (Exception exception) {
            log.error("주차 라벨 생성이 실패했습니다. labelName= {}, exception = {} ", weekNumberLabelName, exception.getMessage());

            notifyCreateNewWeekNumberLabelFailResult(weekNumberLabelName, exception);
        }
    }

    private void notifyCreateNewWeekNumberLabelSuccessResult(String weekNumberLabelName) {
        String newLabelCreationSuccessMessage = notificationService.createNewLabelCreationSuccessMessage(
            weekNumberLabelName);

        notificationService.sendMessage(newLabelCreationSuccessMessage);
    }

    private void notifyCreateNewWeekNumberLabelFailResult(String weekNumberLabelName, Exception exception) {
        String newLabelCreationFailureMessage = notificationService.createNewLabelCreationFailureMessage(
            weekNumberLabelName, exception);

        notificationService.sendMessage(newLabelCreationFailureMessage);
    }

    /**
     * createNewWeeklyReviewIssues는 해당 년과 주차에 해당하는 주간회고 Issue를 일괄 생성하는 메서드이다.
     */
    public void createNewWeeklyReviewIssues(int year, int weekNumber) {
        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        log.info("주간 회고 Issue 생성 Job 을 시작합니다. weekNumberLabelName = {} ", weekNumberLabelName);

        try {
            GithubJobResult githubJobResult = githubIssueJobService.batchCreateNewWeeklyReviewIssuesJob(ReviewStudyInfo.MEMBERS, year, weekNumber);

            log.info("주간 회고 Issue 생성 Job 이 성공했습니다. weekNumberLabelName={}", weekNumberLabelName);

            notifyCreateNewWeeklyReviewIssuesResult(weekNumberLabelName, githubJobResult);
        } catch (IsWeekNumberLabelPresentFailException isWeekNumberLabelPresentFailException) {
            log.error("주간 회고 Issue 생성 Job 실패(원인 : 라벨 존재 여부 파악 실패) : weekNumberLabelName={}, exception={}", weekNumberLabelName, isWeekNumberLabelPresentFailException.getMessage());

            String isWeekNumberLabelPresentFailMessage = notificationService.createIsWeekNumberLabelPresentFailMessage(weekNumberLabelName, isWeekNumberLabelPresentFailException);

            notificationService.sendMessage(isWeekNumberLabelPresentFailMessage);
        } catch (Exception exception) {
            log.error("주간 회고 Issue 생성 Job 실패(원인 : 예상치 못한 예외 발생) : weekNumberLabelName={}, exception={}", weekNumberLabelName, exception.getMessage());

            String unexpectedIssueCreationFailureMessage = notificationService.createUnexpectedIssueCreationFailureMessage(weekNumberLabelName, exception);

            notificationService.sendMessage(unexpectedIssueCreationFailureMessage);
        }
    }

    private void notifyCreateNewWeeklyReviewIssuesResult(String weekNumberLabelName, GithubJobResult githubJobResult) {
        List<GithubIssueApiSuccessResult> githubApiSuccessResults = githubJobResult.successItems().stream().map(githubApiTaskResult -> (GithubIssueApiSuccessResult) githubApiTaskResult.taskResult()).toList();

        List<GithubIssueApiFailureResult> githubIssueApiFailureResults = githubJobResult.failItems().stream().map(githubApiTaskResult -> (GithubIssueApiFailureResult) githubApiTaskResult.taskResult()).toList();

        // 성공 결과 모음
        String successResult = githubApiSuccessResults.isEmpty()
            ? ""
            : githubApiSuccessResults.stream()
                .map(result -> notificationService.createNewIssueCreationSuccessMessage(weekNumberLabelName, result))
                .collect(Collectors.joining("\n"));

        // 실패 결과 모음
        String failureResult = githubIssueApiFailureResults.isEmpty()
            ? ""
            : githubIssueApiFailureResults.stream()
                .map(result -> notificationService.createNewIssueCreationFailureMessage(weekNumberLabelName, result))
                .collect(Collectors.joining("\n"));

        // 최종 결과
        String finalResult = successResult+"\n\n"+failureResult;

        // Discord 로 보내기
        notificationService.sendMessage(finalResult);
    }

    /**
     * closeWeeklyReviewIssues는 해당 년도의 주차에 해당하는 주간회고 Issue 를 일괄 Close 하는 메서드이다.
     */
    public void closeWeeklyReviewIssues(int year, int weekNumber) {
        String labelNameToClose = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        log.info("주간 회고 Issue Close Job 을 시작합니다. weekNumberLabelName = {} ", labelNameToClose);

        try {

            GithubJobResult githubJobResult = githubIssueJobService.batchCloseWeeklyReviewIssuesJob(labelNameToClose);

            log.info("주간 회고 Issue Close Job 성공했습니다. weekNumberLabelName = {} ", labelNameToClose);

            notifyCloseWeeklyReviewIssueResult(labelNameToClose, githubJobResult);

        } catch (GetIssuesToCloseFailException getIssuesToCloseFailException) {
            log.error("주간 회고 Issue Close Job 실패(원인 : Close할 Issue 목록 가져오기 실패) : labelNameToClose={}, exception={}", labelNameToClose, getIssuesToCloseFailException.getMessage());

            String issueFetchFailureMessage = notificationService.createIssueFetchFailureMessage(labelNameToClose, getIssuesToCloseFailException);

            notificationService.sendMessage(issueFetchFailureMessage);

        } catch (IssuesToCloseIsEmptyException issuesToCloseIsEmptyException) {
            log.error("주간 회고 Issue Close Job 실패(원인 : Close 할 Issue 없음) : labelNameToClose={}, exception={}", labelNameToClose, issuesToCloseIsEmptyException.getMessage());

            String emptyIssuesToCloseMessage = notificationService.createEmptyIssuesToCloseMessage(labelNameToClose);

            notificationService.sendMessage(emptyIssuesToCloseMessage);

        } catch (Exception exception) {
            log.error("주간 회고 Issue Close Job 실패(원인 : 예상치 못한 예외 발생) : labelNameToClose={}, exception={}", labelNameToClose, exception.getMessage());

            String unexpectedIssueCloseFailureMessage = notificationService.createUnexpectedIssueCloseFailureMessage(labelNameToClose, exception);

            notificationService.sendMessage(unexpectedIssueCloseFailureMessage);
        }
    }

    private void notifyCloseWeeklyReviewIssueResult(String labelNameToClose, GithubJobResult githubJobResult) {

        List<GithubIssueApiSuccessResult> githubApiSuccessResults = githubJobResult.successItems().stream().map(githubApiTaskResult -> (GithubIssueApiSuccessResult) githubApiTaskResult.taskResult()).toList();

        List<GithubIssueApiFailureResult> githubIssueApiFailureResults = githubJobResult.failItems().stream().map(githubApiTaskResult -> (GithubIssueApiFailureResult) githubApiTaskResult.taskResult()).toList();

        // 성공 결과 모음
        String successResult = githubApiSuccessResults.isEmpty()
            ? ""
            : githubApiSuccessResults.stream()
                .map(result -> notificationService.createIssueCloseSuccessMessage(
                    labelNameToClose, result))
                .collect(Collectors.joining("\n"));

        // 실패 결과 모음
        String failureResult = githubIssueApiFailureResults.isEmpty()
            ? ""
            : githubIssueApiFailureResults.stream()
                .map(result -> notificationService.createIssueCloseFailureMessage(
                    labelNameToClose, result))
                .collect(Collectors.joining("\n"));

        // 최종 결과
        String finalResult = successResult + "\n\n" + failureResult;

        // Discord 로 보내기
        notificationService.sendMessage(finalResult);
    }
}