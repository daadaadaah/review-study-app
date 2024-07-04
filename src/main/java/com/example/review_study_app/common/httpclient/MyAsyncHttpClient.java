package com.example.review_study_app.common.httpclient;

import reactor.core.publisher.Mono;

@Deprecated
public interface MyAsyncHttpClient {

    <T, L> L get(MyHttpRequest request);

    <K> Mono<String> post(MyHttpRequest request);

    <M> Mono<String> patch(MyHttpRequest request);
}
