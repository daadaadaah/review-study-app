package com.example.review_study_app.github;

import java.util.List;

/**
 * GithubIssueService 는 Github 이랑 통신하는 책임을 정의해놓은 인터페이스이다.
 *
 * < 되도록 Exception을 throw 하도록 구현 이유 >
 * - 발생한 Exception에 따라 디스코드로 알림을 보내줘야 하는데, 여기서 그 책임을 담당하면, 로직이 복잡해지므로,
 * - 여러 Service 들간의 조율을 담당하는 ReviewStudySchedulerFacade 에서 처리하도록 하기 위함이다.
 * - 만약, 디스코드로 알림을 보내주지 않고, log만 남겨도 되는 상황의 메서드라면, Excpetion을 throw 하지 않을 것 같다.
 */
public interface GithubIssueService {

    NewLabelName createNewLabel(int year, int weekNumber) throws Exception;

    boolean isWeekNumberLabelPresent(String labelName) throws Exception;

    NewIssue createNewIssue(int currentYear, int currentWeekNumber, String memberFullName, String memberGithubName) throws Exception;

    List<IssueToClose> getIssuesToClose(String labelNameToClose) throws Exception;

    void closeIssue(int issueNumber) throws Exception;
}
