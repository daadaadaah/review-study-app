package com.example.review_study_app.common.httpclient;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.utils.MyDateUtils;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * 어떤 로직에서
 * 언제, 어떤 상황에서 재시도가 발생하고,
 * 재시도 몇번 만에 성공하는지
 *
 */
@Slf4j
@Component
public class MyRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

        String className = (String) context.getAttribute("className");
        String methodName = (String) context.getAttribute("methodName");

        // 재시도 시도 후 호출됨 (성공 또는 실패)
        if (throwable == null) {
            log.info("className={}, methodName={} : Retry successful after {} attempts, now = {}", className, methodName, context.getRetryCount(), MyDateUtils.getNow(ZonedDateTime.now(ZONE_ID_SEOUL)));
        } else {
            log.error("className={}, methodName={} : Retry failed after {} attempts, now = {}", className, methodName, context.getRetryCount(), MyDateUtils.getNow(ZonedDateTime.now(ZONE_ID_SEOUL)));
        }
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

        String className = (String) context.getAttribute("className");
        String methodName = (String) context.getAttribute("methodName");

        // 재시도 중 오류 발생 시 호출됨
        log.warn("className={}, methodName={} : Retry attempt {} failed due to {}. Retrying..., now = {}", className, methodName, context.getRetryCount(), throwable.getMessage(), MyDateUtils.getNow(ZonedDateTime.now(ZONE_ID_SEOUL)));
    }
}
