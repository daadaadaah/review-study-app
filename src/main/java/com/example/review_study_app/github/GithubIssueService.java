package com.example.review_study_app.github;

import com.example.review_study_app.reviewstudy.ReviewStudyInfo;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GithubIssueService {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private String REPOSITORY_NAME = ReviewStudyInfo.REPOSITORY_NAME;

    private GitHub github;

    private GHRepository repo;

    private void connectGithub() throws IOException {
        this.github = GitHub.connectUsingOAuth(GITHUB_OAUTH_ACCESS_TOKEN);
        this.repo = github.getRepository(REPOSITORY_NAME);
    }

    /**
     * 새로운 주차 Label 생성하는 함수
     *
     * < 재시도 하는 이유 >
     * 이 기능은 새로운 주차 Label을 생성하는 함수인다.
     * 이번주차 Label이 생성되어야, 이번주차 issue가 생성되는 상황이다.
     * 따라서, 일시적 장애 또는 간할적으로 발생하는 장애로, 라벨 생성이 되지 않는다면, issue 생성에도 문제가 생길 것이다.
     * 따라서, 되도록 성공 가능성을 높이기 위해 재시도 전략을 선택했다.
     * 또한, 새로운 Label 생성은 API가 멱등성이 보장해주는 API 이므로, 여러번 요청해도 데이터 일관성이 깨지지 않는다.
     *
     * 또한, 재시도로 인해 배치 작업 시간이 길어지더라도, 최대 n 초 이다.
     *
     * # 요청 시나리오(타임아웃 10초와 재시도 2번, 지수 백오프 2초로 설정했을 때)
     * 1. 최초 요청
     * - 타임아웃: 10초
     * - 실패 시 다음 재시도로 이동
     *
     * 2. 첫 번째 재시도
     * - 대기 시간: 2초 (지수 백오프)
     * - 타임아웃: 10초
     * - 실패 시 다음 재시도로 이동
     *
     * 3. 두 번째 재시도
     * - 대기 시간: 4초 (지수 백오프)
     * - 타임아웃: 10초
     * - 실패 시 재시도 종료
     *
     * # 최대 소요 시간 계산
     * 1. 최초 요청: 10초 (타임아웃 발생)
     * 2. 첫 번째 재시도 전 대기: 2초
     * 3. 첫 번째 재시도: 10초 (타임아웃 발생)
     * 4. 두 번째 재시도 전 대기: 4초
     * 5. 두 번째 재시도: 10초 (타임아웃 발생)
     *
     * => 총 36초
     *
     *
     * 따라서, 다음 배치 작업에 영향을 주지 않는다.
     *
     */
    @Retryable(
        retryFor = IOException.class,
        maxAttempts = 2,
        backoff = @Backoff(delay = 2000)
    )
    public String createNewLabel(int year, int weekNumber) throws IOException {
        connectGithub();

        String labelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        String labelColor = ReviewStudyInfo.THIS_WEEK_NUMBER_LABEL_COLOR;

        String labelDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

        GHLabel ghLabel = repo.createLabel(labelName, labelColor, labelDescription);

        return ghLabel.getName();
    }

    /***
     * 라벨 존재 여부 판단 함수
     *
     * < 재시도 하는 이유 >
     * 이 함수는 새로운 issue 생성 전에 issue 에 할당할 라벨이 존재하는지 여부를 판단하는 함수이다.
     * 따라서, 일시적 장애 또는 간할적 장애로 실패시 새로운 issue 생성 작업
     * 따라서, 이것도 성공 가능성이 높은게 좋을 것 같다.
     *
     * 멱등성이 보장
     * 그리고, 조회 API이므로, 여러번 시도해도 데이터의 일관성이 깨지지 않으므로, 재시도 하기로 결정했다.
     *
     *
     *
     */
    @Retryable(
        retryFor = IOException.class,
        maxAttempts = 2,
        backoff = @Backoff(delay = 1000)
    )
    public boolean isWeekNumberLabelPresent(String labelName) {
        try {
            connectGithub();

            repo.getLabel(labelName); // TODO :
            log.info("라벨이 존재합니다. labelName = {}", labelName);

            return true;
        } catch (Exception exception) { // TODO : 존재하지만, 500번대 상황인 경우에도, 라벨이 존재하지 않는걸로 되어 있다. 수정 필요!

            log.warn("라벨이 존재하지 않습니다. exception = {}, labelName = {}", exception.getMessage(), labelName);
            return false;
        }
    }

    /**
     * 새로운 주간회고 Issue 생성하는 함수
     *
     * < 재시도 하는 이유 >
     * - Github 이슈 생성 API는 멱등성이 보장되지 않는 API이다.
     * - 재시도에 의해 예기치 못하게 중복 생성 후 수작업 비용 vs 실패시 수작업 비용
     * - 전자보다 후자가 더 비용이 적게 든다고 판단했다.
     *
     * - 재시도에 의해 예기치 못하게 중복 생성되었을 때, 필요한 작업은
     * 1. 중복된 이슈 찾아서,
     * 2. 해당 이슈 삭제하는 것이다.
     * 그런데, 이슈 삭제는 관리자만 되므로, 팀원들이 이슈 관리 하기 번거로움. 이슈 클로즈 시키면 되지만, 이게 추후에 주가회고 통계 낼 때, 중복 데이터가 들어가는 문제도 발생할 수 있음
     *
     * - 반면, 실패시, 이슈 생성하기 위해서는 템플릿을 활용하면 되니까 좀더 낫다. 날짜 계산은 에러 메시지에서 참고해서 하면 된다!
     *
     *
     *
     */
    public GHIssue createNewIssue(
        int currentYear,
        int currentWeekNumber,
        String memberFullName,
        String memberGithubName
    ) throws IOException {
        connectGithub();

        String issueTitle = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(currentYear, currentWeekNumber, memberFullName);

        String issueBody = ReviewStudyInfo.WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE;

        String thisWeekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(currentYear, currentWeekNumber);

        return repo.createIssue(issueTitle)
            .assignee(memberGithubName)
            .label(thisWeekNumberLabelName)
            .body(issueBody)
            .create();
    }

    /**
     * Close 할 이슈 목록 가져오는 함수
     *
     * < 재시도 하는 이유 >
     * 1. 조회, 이걸 해야 이슈 Close 성공 한다. 간헐적으로 발생하는 일시적 오류로, 이슈 Close 실패하는 것보다 재시도 하는게 더 낫다고 판단함.
     * 2. 조회 API 이므로, 여러번 재시도 한다고 해서 데이터 일관성이 깨지는 것도 아님
     *
     *
     */
    @Retryable(
        retryFor = IOException.class,
        maxAttempts = 2,
        backoff = @Backoff(delay = 1000)
    )
    public List<GHIssue> getIssuesToClose(String labelNameToClose) throws IOException {
        connectGithub();

        return repo.getIssues(GHIssueState.OPEN)
            .stream()
            .filter(ghIssue -> ghIssue.getLabels().stream()
                .anyMatch(label -> label.getName().equals(labelNameToClose))
            )
            .toList();
    }

    /**
     * 이슈 close 하는 함수
     *
     * < 재시도 하는 이유 >
     * 이슈 Close가 멱등성이 보장된다.
     *
     */
    @Retryable(
        retryFor = IOException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public void closeIssue(int issueNumber) throws IOException {
        connectGithub();

        repo.getIssue(issueNumber).close();
    }
}