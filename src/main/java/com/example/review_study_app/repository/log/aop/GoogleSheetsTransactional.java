package com.example.review_study_app.repository.log.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * GoogleSheetsTransactional 은 Google Sheets 와의 데이터 연동 시 트랜젹선 처리가 필요한 메서드에 붙이는 어노테이션이다.
 * Google Sheets 에서 자체적으로 제공하는 트랜잭션 기능이 없어서 만들었다.
 *
 * < 필요한 이유 >
 * - Google Sheets와의 데이터 연동 시 트랜잭션 처리를 통해 데이터의 일관성을 유지합니다.
 * - 트랜잭션 실패 시 롤백 메커니즘을 적용하여 데이터 무결성을 보장합니다.
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GoogleSheetsTransactional {

}
