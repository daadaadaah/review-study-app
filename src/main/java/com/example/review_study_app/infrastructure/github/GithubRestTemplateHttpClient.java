package com.example.review_study_app.infrastructure.github;

import static com.example.review_study_app.domain.ReviewStudyInfo.getRepositoryName;

import com.example.review_study_app.common.enums.ProfileType;
import com.example.review_study_app.common.dto.MyHttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class GithubRestTemplateHttpClient { // TODO : Github API limit 과 관련한 유효성 검사 로직 추가해야 함

    @Value("${spring.profiles.active}")
    private String ACTIVE_PROFILES;

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private static final String GITHUB_BASE_API_URL = "https://api.github.com/repos/";

    private static final String GITHUB_BASE_URL = "https://github.com/";

    private final RestTemplate restTemplate;

    @Autowired
    public GithubRestTemplateHttpClient(
        @Qualifier("githubRestTemplate") RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }

    private static String createGithubApiUrl(String endpoint, ProfileType profileType) {
        return GITHUB_BASE_API_URL + getRepositoryName(profileType) + "/" + endpoint;
    }

    public static String createRepositoryUrl(String path, ProfileType profileType) {
        return GITHUB_BASE_URL + getRepositoryName(profileType) + "/" + path;
    }

    public static String createLabelUrl(ProfileType profileType) {
        return createRepositoryUrl("labels", profileType);
    }

    public static String createIssueUrl(ProfileType profileType, int issueNumber) {
        return createRepositoryUrl("issues/" + issueNumber, profileType);
    }

    private HttpHeaders createCommonHttpHeaders() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; utf-8");
        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN));

        return httpHeaders;
    }

    public <T> MyHttpResponse post(String endpoint, T body) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            createGithubApiUrl(endpoint, ProfileType.fromString(ACTIVE_PROFILES)),
            HttpMethod.POST,
            new HttpEntity<>(body, createCommonHttpHeaders()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }

    public MyHttpResponse get(String endpoint) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            createGithubApiUrl(endpoint, ProfileType.fromString(ACTIVE_PROFILES)),
            HttpMethod.GET,
            new HttpEntity<>(null, createCommonHttpHeaders()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }

    public <T> MyHttpResponse patch(String endpoint, T body) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
            createGithubApiUrl(endpoint, ProfileType.fromString(ACTIVE_PROFILES)),
            HttpMethod.PATCH,
            new HttpEntity<>(body, createCommonHttpHeaders()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}