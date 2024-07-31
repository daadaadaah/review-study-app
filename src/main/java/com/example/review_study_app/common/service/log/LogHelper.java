package com.example.review_study_app.common.service.log;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * LogHelper 는 log에서 자주 사용될만한 함수들을 모아놓은 클래스이다.
 *
 * < UUID로 한 이유>
 * - long 으로 했을 때, id가 겹치는 문제 발생 해서 UUID로 함.
 *
 * < 각 배치 프로세스의 ID 를 ThreadLocal에 저장하는 이유 >
 * - 각 배치 프로세스의 ID가 필요한대, 이걸 각 메서드의 매개변수로 관리하게 되면, 메서드 시그니처가 많아짐
 * - 또한, 한번에 1개의 스케줄러만 동작하므로 단일 스레드 환경이다.
 * - 따라서,ThreadLocal의 값은 스레드에 의해 고유하게 유지되므로, 이 경우 스레드 독립성이 보장되어, 데이터 충돌 우려가 없다.
 * - 따라서, 코드의 간결성과 스레드 독립성이라는 장점을 가지고 있으므로,
 *
 */
@Component
public class LogHelper {

    private final Environment environment;

    private static final ThreadLocal<UUID> threadLocalJobId = new ThreadLocal<>();

    private static final ThreadLocal<UUID> threadLocalStepId = new ThreadLocal<>();

    private static final ThreadLocal<UUID> threadLocalTaskId = new ThreadLocal<>();

    @Autowired
    public LogHelper(
        Environment environment
    ) {
        this.environment =environment;
    }

    public void setJobId() {
        UUID uuid = UUID.randomUUID();

        threadLocalJobId.set(uuid);
    }

    public UUID getJobId() {
        return threadLocalJobId.get();
    }

    public void clearJobId() {
        threadLocalJobId.remove();
    }

    public void setStepId() {
        UUID uuid = UUID.randomUUID();

        threadLocalStepId.set(uuid);
    }

    public UUID getStepId() {
        return threadLocalStepId.get();
    }

    public void clearStepId() {
        threadLocalStepId.remove();
    }

    public void setTaskId() {
        UUID uuid = UUID.randomUUID();

        threadLocalTaskId.set(uuid);
    }

    public UUID getTaskId() {
        return threadLocalTaskId.get();
    }

    public void clearTaskId() {
        threadLocalTaskId.remove();
    }

    public String getEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();

        String environment = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "";

        return environment;
    }

    public String getCreatedAt(long currentTimeMillis) {
        return getNowInSeoul(currentTimeMillis).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private ZonedDateTime getNowInSeoul(long currentTimeMillis) {
        return Instant.ofEpochMilli(currentTimeMillis).atZone(ZONE_ID_SEOUL);
    }
}
