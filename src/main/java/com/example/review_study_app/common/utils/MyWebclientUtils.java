package com.example.review_study_app.common.utils;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Slf4j
public class MyWebclientUtils {

    public static RetryBackoffSpec createRetrySpec(long retryMaxAttempts, Duration backoffDuration) {
        return Retry.backoff(retryMaxAttempts, backoffDuration)
            .filter(throwable -> {
                log.info("재시도하나?");

                if (throwable instanceof WebClientResponseException) {
                    WebClientResponseException ex = (WebClientResponseException) throwable;

                    HttpStatusCode httpStatusCode = ex.getStatusCode();

                    return isRetryableHttpStatusCode(httpStatusCode);
                }
                return false;
            })
            .doBeforeRetry(retrySignal -> {
                log.info("[Retry 전] 재시도 횟수: " + retrySignal.totalRetries()+1+"번째 재시도 중....");
            })
            .doAfterRetry(retrySignal -> {
                // TODO : 나중에 의사결정하기 위해 몇번 재시도 만에 성공했는지 데이터화 해놓으면 좋을듯
                log.info("[Retry 후] 재시도 횟수: " + retrySignal.totalRetries());
            })
            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                log.info("이건 뭐지? : "+retryBackoffSpec);

                log.info("총 재시도 횟수: " + retrySignal.totalRetries());

                log.info("에러럴 : "+retrySignal.failure().getMessage());
                return retrySignal.failure();
            });
    }

    private static boolean isRetryableHttpStatusCode(HttpStatusCode statusCode) {
        return statusCode.is5xxServerError() || statusCode.value() == 429 || statusCode.value() == 422;
    }

}
