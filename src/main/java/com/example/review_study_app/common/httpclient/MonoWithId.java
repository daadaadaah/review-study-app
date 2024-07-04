package com.example.review_study_app.common.httpclient;

import reactor.core.publisher.Mono;

public record MonoWithId<T> (
    Mono<T> mono,
    String identifier
) {

}
