package com.example.review_study_app.common.httpclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 *
 * 기본은 retrieve() 사용하는 걸로
 * webclient 를 사용하는 쪽에서 block 할지, subscribe 할지 결정하도록 하는게 좀더 유연할 것 같다고 판단하여, 대부분의 return 값을 Mono나 Flux로 하였다.
 * 다만, get의 경우, 기본 block으로 함. 왜냐하면, get의 경우 비동기 통신 보다 동기 통신을 더 많이 사용할 것 같다고 판단해서
 */
@Component
public class WebClientHttpClient {

    private final WebClient webClient;

    @Autowired
    public WebClientHttpClient(WebClient.Builder webClient) {
        this.webClient = webClient.build();
    }

    public MyHttpResponse get(MyHttpRequest request) {
        ResponseEntity<String> responseEntityMono =  webClient.get()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .retrieve()
            .toEntity(String.class)
            .block();

        int statusCode = responseEntityMono.getStatusCode().value();
        HttpHeaders headers = responseEntityMono.getHeaders();
        String body = responseEntityMono.getBody();

        return new MyHttpResponse(statusCode, headers, body);
    }

    public <T> ResponseEntity<T> getEntity(MyHttpRequest request, Class<T> clazz) {
        ResponseEntity<T> responseEntity =  webClient.get()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .retrieve()
            .toEntity(clazz)
            .block();

        return responseEntity;
    }

    public <T> T getMono(MyHttpRequest request, Class<T> clazz) {
        T body =  webClient.get()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .retrieve()
            .bodyToMono(clazz)
            .block();

        return body;
    }

    public <T> Flux<T> getFlux(MyHttpRequest request, Class<T> clazz) {
        Flux<T> body =  webClient.get()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .retrieve()
            .bodyToFlux(clazz);

        return body;
    }


    public Mono<String> post(MyHttpRequest request) {
        return webClient.post()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(request.body()), request.body().getClass())
            .retrieve()
            .bodyToMono(String.class);
    }

    public Mono<String> patch(MyHttpRequest request) {
        return webClient.patch()
            .uri(request.url())
            .headers(header -> header.addAll(request.headers()))
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(request.body()), request.body().getClass())
            .retrieve()
            .bodyToMono(String.class);
    }
}
