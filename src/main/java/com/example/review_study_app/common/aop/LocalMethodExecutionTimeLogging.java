package com.example.review_study_app.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * LocalMethodExecutionTimeLogging 은 local 환경에서 특정 메서드의 실행시간을 알고 싶을 때 사용하는 어노테이션이다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LocalMethodExecutionTimeLogging {

}
