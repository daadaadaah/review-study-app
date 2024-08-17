package com.example.review_study_app.infrastructure.github.config;

import com.example.review_study_app.infrastructure.github.interceptor.GitHubApiLoggingInterceptor;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GithubRestTemplateConfig {

    private final GitHubApiLoggingInterceptor gitHubApiLoggingInterceptor;

    @Autowired
    public GithubRestTemplateConfig(GitHubApiLoggingInterceptor gitHubApiLoggingInterceptor) {
        this.gitHubApiLoggingInterceptor = gitHubApiLoggingInterceptor;
    }

    @Bean(name = "githubRestTemplate")
    public RestTemplate githubRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        /**
         * < 추가한 이유 >
         * - @see com.example.review_study_app.infrastructure.resttemplate.discord.config.DiscordRestTemplateConfig
         * - 추가로, 위와 달리, SimpleClientHttpRequestFactory는 제거한 이유는
         * - HttpComponentsClientHttpRequestFactory와 SimpleClientHttpRequestFactory는 둘 다 ClientHttpRequestFactory의 구현체이며, 동시에 사용할 필요가 없기 때문에
         */
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();

        /**
         * 인터셉터를 추가하면서 필요했기 때문이다.
         * 왜냐하면, 요청 및 응답 본문은 스트림이므로, 1번만 읽을 수 있다.
         * 따라서, BufferingClientHttpRequestFactory 를 추가하여, 요청 및 응답 본문을 버퍼에 담아두어 여러 번 읽을 수 있게 했다.
         * 만약, 이걸 추가하지 않으면, 로깅을 위해 요청 및 응답 본문을 1번 사용해서 소멸되었으므로, 비즈니스 로직을 제대로 수행할 수 없다.
         */
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(httpRequestFactory));
        restTemplate.setInterceptors(Collections.singletonList(gitHubApiLoggingInterceptor));
        return restTemplate;
    }
}
