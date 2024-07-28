package com.example.review_study_app.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;


/**
 * < 따로 클래스로 뺀 이유 >
 *
 * 기존에는 Aspect 클래스의 inner 클래스로 관리되고 있었다.
 * 그런데, 이렇게 할 경우, 빈으로 등록되지 않아, Spring이 Spring이 조건을 평가할 때 해당 조건 클래스를 제대로 인식하지 못할 수 있다.
 * 따라서, inner 클래스로 하려면, static으로 선언해주거나 외부 클래스로 추출해야 한다.
 * static 보다 외부 클래스로 추출하는게 더 안정적인 것 같아서, 외부로 추출함
 */

@Slf4j
public class LocalEnvironmentCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String env = context.getEnvironment().getProperty("spring.profiles.active");

        log.info("현재 환경 : active={}", env);

        return "local".equalsIgnoreCase(env);
    }
}
