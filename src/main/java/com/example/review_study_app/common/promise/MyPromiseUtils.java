package com.example.review_study_app.common.promise;

import com.example.review_study_app.common.httpclient.MonoWithId;
import com.example.review_study_app.github.MyApiResult;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
public class MyPromiseUtils {


    // TODO : 400대 에러면, 넘어가고 -> Noti, 500대 에러면, 바로 거기서부터 요청 금지! -> Noti
    // TODO : -> 이게 가능한가? 이미 다 요청을 보낸거 아니야?
    public static <T> Mono<List<MyApiResult<T>>> promiseAllSettled(MonoWithId<T>... monoWithIds) {
        return Flux.merge(
                Arrays
                    .stream(monoWithIds)
                    .map(wrapper -> wrapper
                        .mono()
                        .map(value -> new MyApiResult<T>(wrapper.identifier(), true, value, null))
                        .timeout(Duration.ofSeconds(10)) // TODO : 어느정도가 적당할까? 일단 10초로 하고, 추후에 변경? 참고) https://chatgpt.com/c/e9992ed2-1637-483d-9c42-7bfe06f1bb20
                        .onErrorResume(e -> {
                            log(wrapper.identifier(), (WebClientResponseException) e);

                            return Mono.just(new MyApiResult<T>(wrapper.identifier(), false, null, e));
                        })
                    )
                    .collect(Collectors.toList()))
            .collectList()
            .subscribeOn(Schedulers.boundedElastic());
    }

    private static void log(String responseIdentifier, WebClientResponseException e) { // TODO : 수정 필요
        log.error("responseIdentifier = {} statusCode = {}, responseBody = {}, headers = {} ", responseIdentifier, e.getRawStatusCode(), e.getResponseBodyAsString(), e.getHeaders());
    }

    public void PromiseAll() {

//        List<Mono<String>> monos = issuesToClose.stream().map(issue -> githubIssueServiceWebclient.closeIssue(issue)
//        ).collect(Collectors.toList());
//
//
//        Mono<List<String>> result = Mono.zip(
//            monos,
//            objects -> Arrays.stream(objects)
//                .map(object -> (String) object)
//                .collect(Collectors.toList())
//        );
//

//        result
//            .doOnError(WebClientResponseException.class, this::handleWebClientResponseException)
//            .subscribe(
//                responses -> {
//                    for (int i = 0; i < responses.size(); i++) {
//                        System.out.println("Response " + (i + 1) + ": " + responses.get(i));
//                    }
//                },
//                error -> log.error("error={}", error.getMessage()),
//                () -> System.out.println("All requests completed")
//            );

    }
}
