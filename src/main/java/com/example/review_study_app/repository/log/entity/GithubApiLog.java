package com.example.review_study_app.repository.log.entity;

import org.springframework.http.HttpHeaders;

/**
 * GithubApiLog 은 Github API 의 요청 및 응답을 log로 저장하기 위한 데이터들을 담는 클래스이다.
 *
 * @param id : 식별자
 * @param environment : 개발환경(예 : local)
 * @param taskType : 작업유형 -> Resttemplate 을 사용하는 메서드 이름
 * @param httpMethod : 요청 메서드(예 : GET)
 * @param url : 요청 url
 * @param requestHeaders : 요청 헤더
 * @param requestBody : 요청 바디
 * @param responseHttpStatusCode : 응답 상태 코드(예 : 200)
 * @param responseHeaders : 응답 헤더
 * @param responseBody : 응답 바디
 * @param responseTime : 요청 ~ 응답까지의 소요 시간
 * @param createdAt : log 만들어진 시간 (예 : 2024-12-11 18:03:12)
 */
public record GithubApiLog(
    long id,
    String environment,
    String taskType,
    String httpMethod,
    String url,
    HttpHeaders requestHeaders,
    String requestBody,
    int responseHttpStatusCode,
    HttpHeaders responseHeaders,
    String responseBody,
    long responseTime,
    String createdAt
    ) {

}
