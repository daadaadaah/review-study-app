package com.example.review_study_app.service.github;

import com.example.review_study_app.service.github.vo.GithubApiTaskResult;
import com.example.review_study_app.service.github.dto.GithubJobResult;
import com.example.review_study_app.service.github.vo.GithubIssueApiFailureResult;
import com.example.review_study_app.service.github.vo.GithubIssueApiSuccessResult;
import com.example.review_study_app.repository.github.GithubIssueRepository;
import com.example.review_study_app.service.github.vo.GithubLabelApiSuccessResult;
import com.example.review_study_app.repository.github.dto.IssueCreateForm;
import com.example.review_study_app.repository.github.dto.IssueToClose;
import com.example.review_study_app.repository.github.dto.LabelCreateForm;
import com.example.review_study_app.repository.github.dto.NewIssue;
import com.example.review_study_app.repository.github.dto.NewLabelName;
import com.example.review_study_app.service.github.exception.GetIssuesToCloseFailException;
import com.example.review_study_app.service.github.exception.IsWeekNumberLabelPresentFailException;
import com.example.review_study_app.service.github.exception.IssuesToCloseIsEmptyException;
import com.example.review_study_app.domain.Member;
import com.example.review_study_app.domain.ReviewStudyInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * GithubIssueJobService 는 여러 Github 작업들을 조율하여 Job 을 책임지는 클래스이다.
 *
 * < 용어 정리 >
 * - 일괄 이슈 생성 상황을 예를 들면, 각 용어는 다음과 같다.
 * (1) Job : 여러 개의 이슈를 만드는 전체 작업
 * (2) Step : Job 의 하위 단계 작업 (예 : Service 계층의 작업(라벨 존재 여부 파악하는 작업, 각각의 이슈 생성하는 작업 등))
 * (3) Task : Step 의 하위 단계 작업 (예 : Repository 계층의 작업(각 단계 별로 Github API 통신 작업), Github의 응답 객체를 우리 비즈니스 로직에 필요한 데이터만 Mapping 하는 작업)
 * (4) Item : 처리된 이슈
 *
 * < 참고 사항 >
 * GithubIssueJobService 는 Job 수행 결과를 로깅을 위해 무조건 GithubJobResult 객체를 return 하도록 한다.
 *
 */
@Slf4j
@Component
public class GithubIssueJobService {

    private final GithubIssueRepository githubIssueRepository;

    @Autowired
    public GithubIssueJobService(
        GithubIssueRepository githubIssueRepository
    ) {
        this.githubIssueRepository = githubIssueRepository;
    }

    /**
     * createNewWeeklyLabelJob 는 새로운 주차 라벨을 생성하는 Job이다.
     */
    public GithubJobResult createNewWeeklyLabelJob(int year, int weekNumber) throws Exception {
        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        LabelCreateForm labelCreateForm = new LabelCreateForm(labelName, labelDescription, labelColor);

        NewLabelName newLabelName = githubIssueRepository.createNewLabelStep(labelCreateForm);

        GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(true, new GithubLabelApiSuccessResult(newLabelName.name()));

        List<GithubApiTaskResult> successItems = Arrays.asList(githubApiTaskResult);

        List<GithubApiTaskResult> failItems = new ArrayList<>();

        return new GithubJobResult(
            successItems,
            failItems
        );
    }

    /**
     * batchCreateNewWeeklyReviewIssuesJob 는 새로운 주차 주간 회고 이슈를 일괄 생성하는 Job 이다.
     */
    public GithubJobResult batchCreateNewWeeklyReviewIssuesJob(List<Member> members, int year, int weekNumber) throws Exception {
        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        boolean isWeekNumberLabelPresent = false;

        try {
            isWeekNumberLabelPresent = githubIssueRepository.isWeekNumberLabelPresentStep(weekNumberLabelName);

            log.info("라벨 존재 여부 파악에 성공했습니다. labelName = {}", weekNumberLabelName);
        } catch (Exception exception) {
            log.error("라벨 존재 여부 파악에 실패했습니다. labelName = {}, exception = {}", weekNumberLabelName, exception.getMessage());

            throw new IsWeekNumberLabelPresentFailException(exception); // 예외 상황 별로 다른 Notification 메시지를 전달하기 위해 커스텀 예외로 만듬
        }

        if(!isWeekNumberLabelPresent) {
            createNewWeeklyLabelJob(year, weekNumber); // 새로운 라벨에 대한 예외는 createNewWeekNumberLabel에서 처리함!
        }

        // 1. 이슈 생성
        List<GithubApiTaskResult> githubApiTaskResults = new ArrayList<>();

        members.stream().forEach(member -> {

            String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(year, weekNumber, member.fullName()); // TODO : 서비스에서만 도메인 객체 알도록 변경 필요

            try {

                String issueBody = member.template();

                List<String> assignees = Arrays.asList(member.githubName());

                List<String> labels =  Arrays.asList(weekNumberLabelName);

                IssueCreateForm issueCreateForm = new IssueCreateForm(
                    issueTitle,
                    issueBody,
                    assignees,
                    labels
                );

                NewIssue newGhIssue = githubIssueRepository.createNewIssueStep(issueCreateForm);

                log.info("새로운 이슈가 생성되었습니다. issueTitle = {}, issueNumber = {} ", newGhIssue.title(), newGhIssue.number());

                GithubIssueApiSuccessResult githubApiSuccessResult = new GithubIssueApiSuccessResult(newGhIssue.number(), issueTitle);

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(true, githubApiSuccessResult);

                githubApiTaskResults.add(githubApiTaskResult);

            } catch (Exception exception) {
                log.error("새로운 이슈 생성이 실패했습니다. issueTitle = {}, exception = {} ", issueTitle, exception.getMessage());

                GithubIssueApiFailureResult githubIssueApiFailureResult = new GithubIssueApiFailureResult(null, issueTitle, exception.getMessage());

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(false, githubIssueApiFailureResult);

                githubApiTaskResults.add(githubApiTaskResult);
            }
        });

        List<GithubApiTaskResult> successItems = githubApiTaskResults.stream().filter(githubApiSuccessResult -> githubApiSuccessResult.isSuccess()).toList();

        List<GithubApiTaskResult> failItems = githubApiTaskResults.stream().filter(githubApiFailureResult -> !githubApiFailureResult.isSuccess()).toList();

        return new GithubJobResult(
            successItems,
            failItems
        );
    }

    /**
     * batchCloseWeeklyReviewIssuesJob 는 지난 주차 주간 회고 이슈를 일괄 Close 하는 Job 이다.
     */
    public GithubJobResult batchCloseWeeklyReviewIssuesJob(String labelNameToClose) throws Exception {
        // 1. 이슈 Close
        List<IssueToClose> closedIssues = new ArrayList<>();

        List<GithubApiTaskResult> githubApiTaskResults = new ArrayList<>();

        try {
            closedIssues = githubIssueRepository.getIssuesToCloseStep(labelNameToClose);

            log.info("Close 할 이슈 목록 가져오기 성공했습니다. labelNameToClose = {} ", labelNameToClose);
        } catch (Exception exception) {
            log.error("Close 할 이슈 목록 가져오는 것을 실패했습니다. labelNameToClose = {}, exception = {}", labelNameToClose, exception.getMessage());

            throw new GetIssuesToCloseFailException(exception); // 예외 상황 별로 다른 Notification 메시지를 전달하기 위해
        }

        if (closedIssues.isEmpty()) {
            log.info("Close 할 이슈가 없습니다. labelNameToClose = {} ", labelNameToClose);

            throw new IssuesToCloseIsEmptyException(); // 예외 상황 별로 다른 Notification 메시지를 전달하기 위해
        }

        log.info("(" + labelNameToClose + ") 주간회고 이슈들 Close 시작합니다.");

        closedIssues.stream().forEach(ghIssue -> {

            int issueNumber = ghIssue.number();

            String issueTitle = ghIssue.title();

            try {
                githubIssueRepository.closeIssueStep(issueNumber);

                log.info("이슈 Close 성공했습니다. issueNumber = {}, issueTitle = {} ", issueNumber,
                    issueTitle);
                GithubIssueApiSuccessResult githubApiSuccessResult = new GithubIssueApiSuccessResult(issueNumber, issueTitle);

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(true, githubApiSuccessResult);

                githubApiTaskResults.add(githubApiTaskResult);
            } catch (Exception exception) {
                log.error("이슈 Close 실패했습니다. issueNumber = {}, issueTitle = {}, exception = {} ",
                    issueNumber, issueTitle, exception.getMessage());

                GithubIssueApiFailureResult githubIssueApiFailureResult = new GithubIssueApiFailureResult(issueNumber, issueTitle, exception.getMessage());

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(false, githubIssueApiFailureResult);

                githubApiTaskResults.add(githubApiTaskResult);
            }
        });

        List<GithubApiTaskResult> successItems = githubApiTaskResults.stream().filter(githubApiSuccessResult -> githubApiSuccessResult.isSuccess()).toList();

        List<GithubApiTaskResult> failItems = githubApiTaskResults.stream().filter(githubApiFailureResult -> !githubApiFailureResult.isSuccess()).toList();

        return new GithubJobResult(
            successItems,
            failItems
        );
    }
}
