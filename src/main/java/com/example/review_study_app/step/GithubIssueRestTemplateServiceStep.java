package com.example.review_study_app.step;

import com.example.review_study_app.task.mapper.GithubApiResponseMapper;
import com.example.review_study_app.step.dto.IssueCloseForm;
import com.example.review_study_app.step.dto.IssueCreateForm;
import com.example.review_study_app.step.dto.IssueToClose;
import com.example.review_study_app.step.dto.LabelCreateForm;
import com.example.review_study_app.step.dto.NewIssue;
import com.example.review_study_app.step.dto.NewLabelName;
import com.example.review_study_app.step.exception.MyJsonParseFailException;
import com.example.review_study_app.task.httpclient.dto.MyHttpRequest;
import com.example.review_study_app.task.httpclient.dto.MyHttpResponse;
import com.example.review_study_app.task.httpclient.RestTemplateHttpClient;
import com.example.review_study_app.domain.ReviewStudyInfo;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientResponseException;

/**
 * GithubIssueRestTemplateService 는 RestTemplate 으로 Github API를 사용하여 Github 과 통신하는 책임이 있는 클래스이다.
 *
 * < 알아두면 좋은 사항 >
 * 1. RestClientResponseException 는 HttpClientErrorException 및 HttpServerErrorException의 부모 클래스이다.
 *
 * 2. 기본적으로 재시도는 서버 측 원인으로 예외가 발생한 경우와 재시도로 인해 데이터 정합성이 깨지지 않는 경우(예 : 멱등성이 보장되는 API)로 했다.
 * 왜냐하면, 클라이언트 측 원인으로 실패한 경우, 계속 동일한 요청으로 재시도 해봤자 결과는 같아서, 재시도의 의미가 없다고 생각했기 때문이다.
 * 다만, 경우에 따라서 HttpClientErrorException 에도 재시도를 했을 때, 통신에 성공할 수 있는 경우(예 : 429 Too Many Requests)가 있을 수 있다.
 * 따라서, 일단, Github 통신시 발생하는 예외들을 log로 남겨 놓고, 그 자료를 토대로 추후에 판단해 보는게 좋겠다.
 *
 */
@Slf4j
@Service
public class GithubIssueRestTemplateServiceStep implements GithubIssueServiceStep {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private RestTemplateHttpClient restTemplateHttpClient;

    private GithubApiResponseMapper githubApiResponseMapper;

    @Autowired
    public GithubIssueRestTemplateServiceStep(
        RestTemplateHttpClient restTemplateHttpClient,
        GithubApiResponseMapper githubApiResponseMapper
    ) {
        this.restTemplateHttpClient = restTemplateHttpClient;
        this.githubApiResponseMapper = githubApiResponseMapper;
    }

    private static String createGithubApiUrl(String path) {
        return "https://api.github.com/repos/" + ReviewStudyInfo.REPOSITORY_NAME + "/" + path;
    }

    private HttpHeaders createCommonHttpHeaders() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; utf-8");
        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN));

        return httpHeaders;
    }

    /**
     * createNewLabel는 새로운 주차 Label 생성하는 함수 이다.
     *
     * > 재시도 전략을 선택한 이유
     *
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
     * 따라서, 다음 배치 작업에 영향을 주지 않는다.
     */
    @Retryable(
        retryFor = HttpServerErrorException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public NewLabelName createNewLabelStep(LabelCreateForm labelCreateForm) throws Exception {
        String labelName = labelCreateForm.name();

        String url = createGithubApiUrl("labels");

        HttpHeaders httpHeaders = createCommonHttpHeaders();

        MyHttpRequest request = new MyHttpRequest(url, httpHeaders, labelCreateForm);

        try {
            MyHttpResponse response = restTemplateHttpClient.post(request);

            NewLabelName newLabelName = githubApiResponseMapper.extractNewLabelNameFromResponseBody(response.body());

            log.info("라벨 생성 성공했습니다. labelName={}", newLabelName.name());

            return newLabelName;
        } catch (RestClientResponseException restClientResponseException) {
            log.error("라벨 생성 실패했습니다. labelName={}, exception={}, StatusCode={}", labelName, restClientResponseException.getMessage(), restClientResponseException.getStatusCode());

            throw restClientResponseException;
        } catch (MyJsonParseFailException jsonParseFailException) {
            log.error("생성된 라벨 parse 실패했습니다. labelName={}, exception={}", labelName, jsonParseFailException.getMessage());

            throw jsonParseFailException;
        } catch (Exception exception) {
            log.error("예상치 못한 예외가 발생했습니다. labelName={}, exception={}", labelName, exception.getMessage());

            throw exception;
        }
    }

    // 라벨 존재 여부 판단 함수
    // HTTP response status code 목록 (참고 : https://docs.github.com/en/rest/issues/labels?apiVersion=2022-11-28#get-a-label--status-codes)

    /**
     * isWeekNumberLabelPresent는 라벨 존재 여부 판단 함수 이다.
     *
     * > 재시도 전략을 선택한 이유
     * 이 함수는 새로운 issue 생성 전에 issue 에 할당할 라벨이 존재하는지 여부를 판단하는 함수이다.
     * 따라서, 일시적 장애 또는 간할적 장애로 실패시 새로운 issue 생성 작업
     * 따라서, 이것도 성공 가능성이 높은게 좋을 것 같다.
     *
     * 멱등성이 보장
     * 그리고, 조회 API이므로, 여러번 시도해도 데이터의 일관성이 깨지지 않으므로, 재시도 하기로 결정했다.
     */
    @Retryable(
        retryFor = HttpServerErrorException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public boolean isWeekNumberLabelPresentStep(String labelName) throws Exception {
        try {
            String url = createGithubApiUrl("labels/"+labelName);

            HttpHeaders httpHeaders = createCommonHttpHeaders();

            MyHttpRequest request = new MyHttpRequest(url, httpHeaders, null);

            MyHttpResponse response = restTemplateHttpClient.get(request);

            log.info("라벨이 존재합니다. labelName = {}", labelName);

            return true;
        } catch (RestClientResponseException restClientResponseException) {

            if(restClientResponseException.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.error("라벨이 존재하지 않습니다. labelName = {}, exception = {}", labelName, restClientResponseException.getMessage());

                return false;
//                 throw restClientResponseException; // 실패 테스트용
            }

            log.error("라벨 존재 여부 파악에 실패했습니다. labelName = {}, exception = {}", labelName, restClientResponseException.getMessage());

            throw restClientResponseException;
        } catch (Exception exception) {
            log.error("예상치 못한 예외 발생으로, 라벨 존재 여부 파악에 실패했습니다. labelName = {}, exception = {}", labelName, exception.getMessage());

            throw exception;
        }
    }

    /**
     * createNewIssue는 새로운 주간회고 Issue 생성하는 함수
     *
     * > 재시도 전략을 선택하지 않는 이유
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
     */
    public NewIssue createNewIssueStep(IssueCreateForm issueCreateForm) throws Exception {
        String issueTitle = issueCreateForm.title();

        String url = createGithubApiUrl("issues");

        HttpHeaders httpHeaders = createCommonHttpHeaders();

        MyHttpRequest request = new MyHttpRequest(url, httpHeaders, issueCreateForm);

        try {

            MyHttpResponse response = restTemplateHttpClient.post(request);

            NewIssue newIssue = githubApiResponseMapper.extractNewIssueFromResponseBody(response.body());

            log.info("이슈 생성 성공했습니다. issueNumber={}, issueTitle={}", newIssue.number(), newIssue.title());

            return newIssue;
        } catch (RestClientResponseException restClientResponseException) {
            log.error("이슈 생성 실패했습니다. issueTitle={}, StatusCode={}, exception={}", issueTitle, restClientResponseException.getStatusCode(),  restClientResponseException.getMessage());

            throw restClientResponseException;
        } catch (MyJsonParseFailException jsonParseFailException) {
            log.error("생성된 이슈 parse 실패했습니다. issueTitle={}, exception={}", issueTitle, jsonParseFailException.getMessage());

            throw jsonParseFailException;
        } catch (Exception exception) {
            log.error("예상치 못한 예외 발생으로, 이슈 생성을 실패했습니다. issueTitle={}, exception={}", issueTitle, exception.getMessage());

            throw exception;
        }
    }

    /**
     * getIssuesToClose는 Close 할 이슈 목록 가져오는 함수 이다.
     *
     * < 재시도 하는 이유 >
     * 1. 조회, 이걸 해야 이슈 Close 성공 한다. 간헐적으로 발생하는 일시적 오류로, 이슈 Close 실패하는 것보다 재시도 하는게 더 낫다고 판단함.
     * 2. 조회 API 이므로, 여러번 재시도 한다고 해서 데이터 일관성이 깨지는 것도 아님
     *
     * < 알아 두면 좋은 사항 >
     * 만약, 현재 open되어 있고, 특정 라벨이 달린 이슈에 해당하는 이슈가 없을 때에는 404가 아닌 정상 응답 200 으로 처리되고,
     * 404 에러는 "issues1?state=open&labels=24년_27주차" 이렇게 잘못된 API 주소일 때 발생함.
     *
     */
    @Retryable(
        retryFor = HttpServerErrorException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public List<IssueToClose> getIssuesToCloseStep(String labelNameToClose) throws Exception {
        // 기본 30개, 최대 100개 -> 나는 10개씩만 가져오게, 회고 스터디의 경우 10명 초과일 경우 팀을 나누는게 좋으므로,
        String url = createGithubApiUrl("issues?per_page=10&state=open&labels="+labelNameToClose);

        HttpHeaders httpHeaders = createCommonHttpHeaders();

        MyHttpRequest request = new MyHttpRequest(url, httpHeaders, null);

        try {
            MyHttpResponse response = restTemplateHttpClient.get(request);

            List<IssueToClose> issuesToClose = githubApiResponseMapper.extractIssueToClosListFromResponseBody(response.body());

            log.info("이슈 조회를 성공했습니다. labelNameToClose={}", labelNameToClose);

            return issuesToClose;
        } catch (RestClientResponseException restClientResponseException) {
            log.error("Github API 통신 실패 : labelNameToClose={} exception={}, StatusCode={}", labelNameToClose, restClientResponseException.getMessage(), restClientResponseException.getStatusCode());

            throw restClientResponseException;
        } catch (MyJsonParseFailException jsonParseFailException) {
            log.error("이슈 목록 parse 실패했습니다. labelNameToClose={}, exception={}", labelNameToClose, jsonParseFailException.getMessage());

            throw jsonParseFailException;
        } catch (Exception exception) {
            log.error("예상치 못한 예외 발생으로, 이슈 조회를 실패했습니다. labelNameToClose={}, exception={}", labelNameToClose, exception.getMessage());

            throw exception;
        }
    }

    /**
     * closeIssue는 이슈 close 하는 함수 이다.
     *
     * < 재시도 하는 이유 >
     * 이슈 Close가 멱등성이 보장된다.
     *
     * < 알아두면 좋은 사항 >
     * - 존재하지 않는 이슈 번호로 요청했을 때, 404 에러 발생함.
     *
     */
    @Retryable(
        retryFor = HttpServerErrorException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000)
    )
    public void closeIssueStep(int issueNumber) throws Exception {
        String url = createGithubApiUrl("issues/"+issueNumber);

        HttpHeaders headers = createCommonHttpHeaders();

        IssueCloseForm issueCloseForm = new IssueCloseForm("close", "completed");

        MyHttpRequest request = new MyHttpRequest(url, headers, issueCloseForm);

        try {
            restTemplateHttpClient.patch(request);

            log.info("Github API 통신 성공 issueNumber={}", issueNumber);
        } catch (Exception exception) {
            log.error("예상치 못한 예외 발생으로, patch 통신 실패했습니다. issueNumber={}, exception={}", issueNumber, exception.getMessage()); // TODO : 예외 로직 추가

            throw exception;
        }
    }
}
