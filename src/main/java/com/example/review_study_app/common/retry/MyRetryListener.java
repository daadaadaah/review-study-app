package com.example.review_study_app.common.retry;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import com.example.review_study_app.common.utils.MyDateUtils;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MyRetryListener implements RetryListener {
    // 맨처음 한번 호출됨 -> Retry를 시작하는 설정이다. true여야만 retry가 실행된다.
    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        String className = (String) context.getAttribute("className");
        String methodName = (String) context.getAttribute("methodName");

        log.info("className={}, methodName={} : before retry. {} attempts, now = {}", className, methodName, context.getRetryCount(), MyDateUtils.getNow(ZonedDateTime.now(ZONE_ID_SEOUL)));
        // 새로운 재시도 시도 전에 호출됨
        return true;
    }

    // 중간에 재시도했지만 실패했을 경우에 호출됨 -> Retry Template에 정의한 exception이 발생하면 실행된다.
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        // 재시도 중 오류 발생 시 호출됨
        String className = (String) context.getAttribute("className");
        String methodName = (String) context.getAttribute("methodName");

        log.warn("className={}, methodName={} : Retry attempt {} failed due to {}. Retrying..., now = {}", className, methodName, context.getRetryCount(), throwable.getMessage(), MyDateUtils.getNow(ZonedDateTime.now(ZONE_ID_SEOUL)));
    }


    // 최대 재시도 횟수 전에 성공하거나 최대 재시도 횟수가 끝난 다음에 호출됨 -> Retry 종료 후에 실행된다.
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
}