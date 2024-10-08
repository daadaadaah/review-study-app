package com.example.review_study_app.service.log.helper;

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
 * - 기존에는 System.currentTimeMillis() 값으로 했었다.
 * - 그런데, 기존 방법으로 했을 때, 겹치는 문제가 발생해서 UUID로 했다.
 * - UUID로 할 경우, 시간순으로 정렬하기가 어렵다는 단점이 있는데, createdAt이라는 필드가 존재하므로, 지금 상황에서는 단점이 되지 않는다고 생각한다.
 * -
 *
 * < 각 배치 프로세스의 ID 를 ThreadLocal에 저장하는 이유 >
 * - 각 배치 프로세스별로 로깅용 AOP에서 각 배치 프로세스의 ID가 필요한 상황이었다.
 * - 이를 해결 하기 위해 2가지 방법을 생각했다.
 *      방법 1. 각각의 배치 프로세스의 ID를 각 메서드의 매개변수에 하나하나 추가해서 매개변수에서 뽑아서 사용하기
 *      방법 2. 각각의 배치 프로세스의 ID를 ThreadLocal에 저장해서 사용하기
 * - 방법 1의 경우, 각 메서드의 매개변수를 한하나하 바꿔줘야 해서 수정의 범위가 커지고, 메서드의 시그니처도 많아지는 단점이 있었다.
 * - 반면, 방법 2의 경우, ThreadLocal에다 저장하고 가져다 사용하면 되므로, 수정의 범위가 방법 1보다 작았다.
 * - 또한, spring의 @Scheduled 애노테이션을 사용하는 경우, 기본적으로 `단일 스레드`로 동작한다.
 * - 따라서, ThreadLocal의 값은 스레드에 의해 고유하게 유지되어 스레드 독립성이 보장되므로, 데이터 충돌 문제가 발생하지 않는다.
 * - 따라서, 코드 수정의 범위가 적으면서도 데이터 정합성을 지키리 수 있는 방법으로 ThreadLocal을 사용했다.
 *
 * - 단, 설정으로 spring의 @Scheduled 애노테이션을 사용하는 경우에도 멀티 스레드로 동작하게 할 수 있지만,
 * - 지금 데이터 수준이랑 스케줄 작업 수준을 고려해볼 때, 멀티 스레드가 필요한 상황이 아니므로, ThreadLocal이 괜찮다고 생각한다.
 * - 만약, 성능을 향상시키고 싶다면, 멀티 스레드가 아닌 비동기로도 해결할 수도 있기 때문에, ThreadLocal로 고고!
 *
 * - 참고로, spring의 @Scheduled를 여러개 동시에 실행시키면, 스레드를 따로 설정해주지 않는 이상, 1개의 스케줄링이 실행되고 완료된 후에 다른 스케줄링이 실행된다.
 * - 즉, 스레드를 따로 설정해주지 않는 한 기본적으로 여러개가 동시에 실행되지 않는다.
 * - 따라서, 스레드 독립성이 보장되어 데이터 정합성을 유지할 수 있고, 코드 수정 범위가 적기 때문에 ThreadLocal을 선택했다.
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

    /** 공통 **/
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

    /** Job **/
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

    /** Step **/
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

    /** Task **/
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
}
