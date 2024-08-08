package com.example.review_study_app.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * LocalMethodExecutionTimeLogging 은 local 환경에서 특정 메서드의 실행시간을 알고 싶을 때 사용하는 어노테이션이다.
 *
 * 기본적으로 실행시간이 log 에 찍히지만, 디스코드 같은 알림 서비스에 보내고 싶으면, isSendNotification 을 활용하면 된다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LocalMethodExecutionTimeLogging {
    boolean isSendNotification() default false;
}
