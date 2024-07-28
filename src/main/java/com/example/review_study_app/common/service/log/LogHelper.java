package com.example.review_study_app.common.service.log;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * LogHelper 는 log에서 자주 사용될만한 함수들을 모아놓은 클래스이다.
 */
@Component
public class LogHelper {

    private final Environment environment;

    @Autowired
    public LogHelper(
        Environment environment
    ) {
        this.environment =environment;
    }

    private ZonedDateTime getNowInSeoul(long currentTimeMillis) {
        return Instant.ofEpochMilli(currentTimeMillis).atZone(ZONE_ID_SEOUL);
    }

    public long getId(long currentTimeMillis) {
        return getNowInSeoul(currentTimeMillis).toInstant().toEpochMilli();
    }

    public String getCreatedAt(long currentTimeMillis) {
        return getNowInSeoul(currentTimeMillis).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();

        String environment = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "";

        return environment;
    }
}
