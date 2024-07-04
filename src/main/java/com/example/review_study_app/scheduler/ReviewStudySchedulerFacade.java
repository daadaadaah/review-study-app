package com.example.review_study_app.scheduler;


import static com.example.review_study_app.common.json.MyJsonUtils.extractFieldFromBody;
import static com.example.review_study_app.common.promise.MyPromiseUtils.promiseAllSettled;
import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.httpclient.MonoWithId;
import com.example.review_study_app.github.GithubApiFailureResult;
import com.example.review_study_app.github.MyApiResult;
import com.example.review_study_app.github.GithubApiSuccessResult;
import com.example.review_study_app.github.GithubIssueService;
import com.example.review_study_app.github.GithubIssueServiceWebclientImpl;
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
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReviewStudySchedulerFacade {

    private final GithubIssueService githubIssueService;

    private final GithubIssueServiceWebclientImpl githubIssueServiceWebclient; // TODO : return 타입을 일관성있게 작성하기 어려워서 클래스로?

    private final NotificationService notificationService;

    @Autowired
    public ReviewStudySchedulerFacade(
        GithubIssueService githubIssueService,
        NotificationService notificationService,
        GithubIssueServiceWebclientImpl githubIssueServiceWebclient
    ) {
        this.githubIssueService = githubIssueService;
        this.notificationService = notificationService;
        this.githubIssueServiceWebclient = githubIssueServiceWebclient;
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
//            githubIssueService.createNewLabel(year, weekNumber);
            githubIssueServiceWebclient.createLabel(year, weekNumber);

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


    /**
     * -> 속도가 중요할 때는 비동기가 낫구,
     * 병렬 요청이 동기식일 때 : 400대 에러 발생
     * 병렬 요청이 비동기식일 때 :
     */

    /**
     * 이번주 주간회고 여러개 Issue를 생성하는 함수
     *
     * TODO
     * [ ] 멤버가 많아지면, 몇명씩 어떻게 분할해서 요청을 보낼 것인지 생각해보기
     * [ ] 이슈 생성 Github API가 멱등성을 보장하지 않는다. 따라서, 중복 체크 해야 될까? 만약, 동일한 타이틀을 가진 이슈가 생성되면, 큰 문제는 없을 것 같다. 그냥 수작업으로 Close 시키면 되지 않을까?
     */
    public void createNewWeeklyReviewIssuesAsync(int year, int weekNumber) {
        ZonedDateTime startTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        String weekNumberLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        List<MonoWithId<String>> monoWithIds = ReviewStudyInfo.MEMBERS.stream().map(member -> {

            Mono<String> monoResponse = githubIssueServiceWebclient.createIssue(year, weekNumber, member.fullName(), member.githubName());

            String identifier = ReviewStudyInfo.getFormattedWeeklyReviewIssueTitle(year, weekNumber, member.fullName());

            return new MonoWithId<>(monoResponse, identifier);
        }).collect(Collectors.toList());

        promiseAllSettled(monoWithIds.toArray(new MonoWithId[0]))
            .subscribe(results -> {
                    List<GithubApiSuccessResult> githubApiSuccessResults = new ArrayList<>();
                    List<GithubApiFailureResult> githubApiFailureResults = new ArrayList<>();

                    // List<MyApiResult<Object>>으로 타입 캐스팅 안하면, 컴파일 단게에서 results을 Object로 인지해서 size() 사용할 수 없다고 나옴, 근데, log로 results.getClass() 출력해보면, List로 나오김함. 뭐가 문제야?
                    for (int i = 0; i < ((List<MyApiResult<Object>>) results).size(); i++) {
                        MyApiResult result = ((List<MyApiResult<Object>>) results).get(i);

                        log.info("identifier = {}, status = {}, value = {},  error = {} ", result.identifier(), result.isSuccess(), result.value(), result.error() != null ? result.error().getMessage() : null);

                        if(result.isSuccess()) {
                            GithubApiSuccessResult githubApiSuccessResult = new GithubApiSuccessResult(
                                extractFieldFromBody((String) result.value(), "number").asInt(), result.identifier());

                            githubApiSuccessResults.add(githubApiSuccessResult);
                        } else {
                            GithubApiFailureResult githubApiFailureResult = new GithubApiFailureResult(null, result.identifier(), result.error().getMessage());

                            githubApiFailureResults.add(githubApiFailureResult);
                        }
                    }

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
                },
                error -> {
                    log.error("error={} ", error);
//                    notificationService.sendMessage(error.getMessage()); // TODO : 이슈 일괄 생성 에러
                },
                () -> {
                    ZonedDateTime endTime = ZonedDateTime.now(ZONE_ID_SEOUL);

                    long durationMillis = java.time.Duration.between(startTime, endTime).toMillis();

                    String executionTimeMessage = notificationService.createExecutionTimeMessage("createNewWeeklyReviewIssuesAsync", durationMillis);

                    notificationService.sendMessage(executionTimeMessage);
                }
            );
    }

    // TODO : 일단, 10명 미만이니, 10명 미만인 상황을 가정하고 구현하고, 추후에 확장성 고려하기
    public void closeWeeklyReviewIssuesAsync(int year, int weekNumber) {
        ZonedDateTime startTime = ZonedDateTime.now(ZONE_ID_SEOUL);

        List<Integer> issuesToClose = new ArrayList<>();

        String labelNameToClose = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

        try {
            issuesToClose = githubIssueServiceWebclient.getIssuesToClose(labelNameToClose);

            log.info("Close 할 이슈 목록 가져오기 성공했습니다. labelNameToClose = {} ", labelNameToClose);

        } catch (Exception exception) {
            log.error("Close 할 이슈 목록 가져오는 것을 실패했습니다. exception = {}", exception.getMessage());

            String issueFetchFailureMessage = notificationService.createIssueFetchFailureMessage(
                labelNameToClose, exception);

            notificationService.sendMessage(issueFetchFailureMessage);
            return;
        }

        System.out.println("issueToClose.size : "+issuesToClose.size());

        if (issuesToClose.isEmpty()) {
            log.info("Close 할 이슈가 없습니다. ");

            String emptyIssuesToCloseMessage = notificationService.createEmptyIssuesToCloseMessage(
                labelNameToClose);

            notificationService.sendMessage(emptyIssuesToCloseMessage);

            return;
        }

        log.info("(" + labelNameToClose + ") 주간회고 이슈 Close 시작");

        // 2. Issue Close 시키기
        List<MonoWithId<String>> monoWithIds = issuesToClose.stream().map(issue -> new MonoWithId<>(
            githubIssueServiceWebclient.closeIssue(issue), String.valueOf(issue))
        ).collect(Collectors.toList());


        promiseAllSettled(monoWithIds.toArray(new MonoWithId[0]))
            .subscribe(results -> {

                    List<GithubApiSuccessResult> githubApiSuccessResults = new ArrayList<>();
                    List<GithubApiFailureResult> githubApiFailureResults = new ArrayList<>();

                    // TODO : List<MyApiResult<Object>>으로 타입 캐스팅 안하면, 컴파일 단게에서 results을 Object로 인지해서 size() 사용할 수 없다고 나옴, 근데, log로 results.getClass() 출력해보면, List로 나오김함. 뭐가 문제야?
                    for (int i = 0; i < ((List<MyApiResult<Object>>) results).size(); i++) {
                        MyApiResult result = ((List<MyApiResult<Object>>) results).get(i);

                        log.info("identifier = {}, status = {}, value = {},  error = {} ", result.identifier(), result.isSuccess(), result.value(), result.error() != null ? result.error().getMessage() : null);

                        if(result.isSuccess()) {
                            GithubApiSuccessResult githubApiSuccessResult = new GithubApiSuccessResult(
                                extractFieldFromBody((String) result.value(), "number").asInt(), result.identifier());

                            githubApiSuccessResults.add(githubApiSuccessResult);
                        } else {
                            GithubApiFailureResult githubApiFailureResult = new GithubApiFailureResult(null, result.identifier(), result.error().getMessage());

                            githubApiFailureResults.add(githubApiFailureResult);
                        }

                    }

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
                },
                error -> {
                    log.error("error={} ", error);
//                    notificationService.sendMessage(error.getMessage()); // TODO : 이슈 일괄 Close 에러
                },
                () -> {
                    ZonedDateTime endTime = ZonedDateTime.now(ZONE_ID_SEOUL);

                    long durationMillis = java.time.Duration.between(startTime, endTime).toMillis();

                    String executionTimeMessage = notificationService.createExecutionTimeMessage("closeWeeklyReviewIssuesAsync", durationMillis);

                    notificationService.sendMessage(executionTimeMessage);
                }
            );
    }
}