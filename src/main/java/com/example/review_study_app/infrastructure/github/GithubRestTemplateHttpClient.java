package com.example.review_study_app.infrastructure.github;

import com.example.review_study_app.domain.ReviewStudyInfo;
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
public class GithubRestTemplateHttpClient {

    @Value("${github.oauth.accessToken}")
    private String GITHUB_OAUTH_ACCESS_TOKEN;

    private static final String BASE_URL = "https://api.github.com/repos/";

    private static String createGithubApiUrl(String endpoint) {
        return BASE_URL + ReviewStudyInfo.REPOSITORY_NAME + "/" + endpoint;
    }

    private HttpHeaders createCommonHttpHeaders() {

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json; utf-8");
        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", GITHUB_OAUTH_ACCESS_TOKEN));

        return httpHeaders;
    }

    private final RestTemplate restTemplate;

    @Autowired
    public GithubRestTemplateHttpClient(
        @Qualifier("githubRestTemplate") RestTemplate restTemplate
    ) {
        this.restTemplate = restTemplate;
    }

    public <T> MyHttpResponse post(String endpoint, T body) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            createGithubApiUrl(endpoint),
            HttpMethod.POST,
            new HttpEntity<>(body, createCommonHttpHeaders()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }

    public MyHttpResponse get(String endpoint) throws Exception {

        ResponseEntity<String> response = restTemplate.exchange(
            createGithubApiUrl(endpoint),
            HttpMethod.GET,
            new HttpEntity<>(null, createCommonHttpHeaders()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }

    public <T> MyHttpResponse patch(String endpoint, T body) throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
            createGithubApiUrl(endpoint),
            HttpMethod.PATCH,
            new HttpEntity<>(body, createCommonHttpHeaders()),
            String.class
        );

        return new MyHttpResponse(response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }
}