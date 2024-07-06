package com.example.review_study_app.common.httpclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
public class WebClientHttpClient {

    private final WebClient webClient;

    @Autowired
    public WebClientHttpClient(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public <T> Mono<T> post(MyHttpRequest request, Class<T> responseType) {
        return webClient.post()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .body(Mono.just(request.body()), request.body().getClass())
            .retrieve()
            .bodyToMono(responseType);
    }
}
