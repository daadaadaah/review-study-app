package com.example.review_study_app.common.utils;


import java.util.UUID;

/**
 * BatchProcessIdContext 은 배치 프로세스의 Id의 생명주기(?)를 관리하는 쿨래스이다.
 * 원래는 각 배치 프로세스의 메서드에 id를 추가하려고 했으나, ThreadLocal로 하면, 수정 범위가 더 작아 ThreadLocal로 구현함.
 *
 * long 으로 했을 때, id가 겹치는 문제 발생 해서 UUID로 함.
 */

public class BatchProcessIdContext {
    private static final ThreadLocal<String> threadLocalJobId = new ThreadLocal<>(); // TODO : 코드 리뷰 요청해보기. 특히, ThreadLocal로 구현했을 때 발생할 수 있는 문제점에 관해
    private static final ThreadLocal<String> threadLocalStepId = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalTaskId = new ThreadLocal<>();

    public static void setJobId(UUID uuid) {
        threadLocalJobId.set(uuid.toString());
    }

    public static String getJobId() {
        return threadLocalJobId.get();
    }

    public static void clearJobId() {
        threadLocalJobId.remove();
    }

    public static void setStepId(UUID uuid) {
        threadLocalStepId.set(uuid.toString());
    }

    public static String getStepId() {
        return threadLocalStepId.get();
    }

    public static void clearStepId() {
        threadLocalStepId.remove();
    }

    public static void setTaskId(UUID uuid) {
        threadLocalTaskId.set(uuid.toString());
    }

    public static String getTaskId() {
        return threadLocalTaskId.get();
    }

    public static void clearTaskId() {
        threadLocalTaskId.remove();
    }
}
