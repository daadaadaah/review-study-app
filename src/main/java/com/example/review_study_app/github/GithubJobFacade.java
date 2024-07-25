package com.example.review_study_app.github;

import com.example.review_study_app.github.exception.GetIssuesToCloseFailException;
import com.example.review_study_app.github.exception.IsWeekNumberLabelPresentFailException;
import com.example.review_study_app.github.exception.IssuesToCloseIsEmptyException;
import com.example.review_study_app.reviewstudy.Member;
import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * GithubJobFacade 는 여러 Github 작업들을 조율하는 책임을 가진 클래스이다.
 *
 * < 용어 정리 >
 * 여러 개의 이슈를 만드는 전체 작업: Job
 * 1개의 이슈를 만드는 개별 작업: Task
 *
 * < 참고 사항 >
 * GithubJobFacade 는 Job 수행 시간 로깅을 위해 무조건 JobResult 객체를 return 하도록 한다.
 *
 */
@Slf4j
@Component
public class GithubJobFacade {

    private final GithubIssueService githubIssueService;

    @Autowired
    public GithubJobFacade( // TODO : 총 수행 시간 로깅하기
        GithubIssueService githubIssueService
    ) {
        this.githubIssueService = githubIssueService;
    }

    private String getMethodName(Thread thread) {
        return thread.getStackTrace()[1].getMethodName();
    }

    public JobResult createNewLabel(int year, int weekNumber) throws Exception {
        long taskId = System.currentTimeMillis();

        String methodName = getMethodName(Thread.currentThread());

        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        LabelCreateForm labelCreateForm = new LabelCreateForm(labelName, labelDescription, labelColor);

        NewLabelName newLabelName = githubIssueService.createNewLabel(labelCreateForm); // TODO : 라벨 이름을 매개변수로 바꾸는 것도 좋을 것 같음

        GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(taskId, true, new GithubLabelApiSuccessResult(newLabelName.name()));

        List<GithubApiTaskResult> successTaskIds = Arrays.asList(githubApiTaskResult);

        List<GithubApiTaskResult> failTaskIds = new ArrayList<>();

        int totalTaskCount = successTaskIds.size() + failTaskIds.size();

        return new JobResult(
            methodName,
            JobStatus.COMPLETED,
            "Job 수행 성공",
            totalTaskCount,
            successTaskIds.size(),
            successTaskIds,
            failTaskIds.size(),
            failTaskIds
        );
    }

    public JobResult batchCreateNewWeeklyReviewIssues(List<Member> members, int year, int weekNumber) throws Exception {
        String methodName = getMethodName(Thread.currentThread());;

        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        boolean isWeekNumberLabelPresent = false;

        try {
            isWeekNumberLabelPresent = githubIssueService.isWeekNumberLabelPresent(weekNumberLabelName);

            log.info("라벨 존재 여부 파악에 성공했습니다. labelName = {}", weekNumberLabelName);
        } catch (Exception exception) {
            log.error("라벨 존재 여부 파악에 실패했습니다. labelName = {}, exception = {}", weekNumberLabelName, exception.getMessage());

            throw new IsWeekNumberLabelPresentFailException(exception);
        }

        if(!isWeekNumberLabelPresent) {
            createNewLabel(year, weekNumber); // 새로운 라벨에 대한 예외는 createNewWeekNumberLabel에서 처리함!
        }

        // 1. 이슈 생성
        List<GithubApiTaskResult> githubApiTaskResults = new ArrayList<>();

        members.stream().forEach(member -> {
            long taskId = System.currentTimeMillis();

            String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(year, weekNumber, member.fullName()); // TODO : 서비스에서만 도메인 객체 알도록 변경 필요

            try {

                String issueBody = ReviewStudyInfo.WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE;

                List<String> assignees = Arrays.asList(member.githubName());

                List<String> labels =  Arrays.asList(weekNumberLabelName);

                IssueCreateForm issueCreateForm = new IssueCreateForm(
                    issueTitle,
                    issueBody,
                    assignees,
                    labels
                );

                NewIssue newGhIssue = githubIssueService.createNewIssue(issueCreateForm);

                log.info("새로운 이슈가 생성되었습니다. issueTitle = {}, issueNumber = {} ", newGhIssue.title(), newGhIssue.number());

                GithubIssueApiSuccessResult githubApiSuccessResult = new GithubIssueApiSuccessResult(newGhIssue.number(), issueTitle);

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(taskId, true, githubApiSuccessResult);

                githubApiTaskResults.add(githubApiTaskResult);

            } catch (Exception exception) {
                log.error("새로운 이슈 생성이 실패했습니다. issueTitle = {}, exception = {} ", issueTitle, exception.getMessage());

                GithubIssueApiFailureResult githubIssueApiFailureResult = new GithubIssueApiFailureResult(null, issueTitle, exception.getMessage());

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(taskId, false, githubIssueApiFailureResult);

                githubApiTaskResults.add(githubApiTaskResult);
            }
        });

        List<GithubApiTaskResult> successTaskIds = githubApiTaskResults.stream().filter(githubApiSuccessResult -> githubApiSuccessResult.isSuccess()).toList();

        List<GithubApiTaskResult> failTaskIds = githubApiTaskResults.stream().filter(githubApiFailureResult -> !githubApiFailureResult.isSuccess()).toList();

        int totalTaskCount = successTaskIds.size() + failTaskIds.size();

        return new JobResult(
            methodName,
            JobStatus.COMPLETED,
            "Job 수행 성공",
            totalTaskCount,
            successTaskIds.size(),
            successTaskIds,
            failTaskIds.size(),
            failTaskIds
        );
    }

    public JobResult batchCloseWeeklyReviewIssues(String labelNameToClose) throws Exception {
        String methodName = getMethodName(Thread.currentThread());;

        // 1. 이슈 Close
        List<IssueToClose> closedIssues = new ArrayList<>();

        List<GithubApiTaskResult> githubApiTaskResults = new ArrayList<>();

        try {
            closedIssues = githubIssueService.getIssuesToClose(labelNameToClose);

            log.info("Close 할 이슈 목록 가져오기 성공했습니다. labelNameToClose = {} ", labelNameToClose);
        } catch (Exception exception) {
            log.error("Close 할 이슈 목록 가져오는 것을 실패했습니다. labelNameToClose = {}, exception = {}", labelNameToClose, exception.getMessage());

            throw new GetIssuesToCloseFailException(exception);
        }

        if (closedIssues.isEmpty()) {
            log.info("Close 할 이슈가 없습니다. labelNameToClose = {} ", labelNameToClose);

            throw new IssuesToCloseIsEmptyException();
        }

        log.info("(" + labelNameToClose + ") 주간회고 이슈들 Close 시작합니다.");

        closedIssues.stream().forEach(ghIssue -> {
            long taskId = System.currentTimeMillis();

            int issueNumber = ghIssue.number();

            String issueTitle = ghIssue.title();

            try {
                githubIssueService.closeIssue(issueNumber);

                log.info("이슈 Close 성공했습니다. issueNumber = {}, issueTitle = {} ", issueNumber,
                    issueTitle);
                GithubIssueApiSuccessResult githubApiSuccessResult = new GithubIssueApiSuccessResult(issueNumber, issueTitle);

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(taskId, true, githubApiSuccessResult);

                githubApiTaskResults.add(githubApiTaskResult);
            } catch (Exception exception) {
                log.error("이슈 Close 실패했습니다. issueNumber = {}, issueTitle = {}, exception = {} ",
                    issueNumber, issueTitle, exception.getMessage());

                GithubIssueApiFailureResult githubIssueApiFailureResult = new GithubIssueApiFailureResult(issueNumber, issueTitle, exception.getMessage());

                GithubApiTaskResult githubApiTaskResult = new GithubApiTaskResult(taskId, false, githubIssueApiFailureResult);

                githubApiTaskResults.add(githubApiTaskResult);
            }
        });

        List<GithubApiTaskResult> successTaskIds = githubApiTaskResults.stream().filter(githubApiSuccessResult -> githubApiSuccessResult.isSuccess()).toList();

        List<GithubApiTaskResult> failTaskIds = githubApiTaskResults.stream().filter(githubApiFailureResult -> !githubApiFailureResult.isSuccess()).toList();

        int totalTaskCount = successTaskIds.size() + failTaskIds.size();

        return new JobResult(
            methodName,
            JobStatus.COMPLETED,
            "Job 수행 성공",
            totalTaskCount,
            successTaskIds.size(),
            successTaskIds,
            failTaskIds.size(),
            failTaskIds
        );
    }
}
