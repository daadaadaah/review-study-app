package com.example.review_study_app.repostiory.log.entity;

import com.example.review_study_app.common.enums.BatchProcessStatus;
import com.example.review_study_app.common.enums.BatchProcessType;
import java.util.UUID;

/**
 *
 * ExecutionTimeLog 는 모든 Job, Step, Task의 실행 시간을 파악하기 위해 필요한 데이터를 담는 클래스이다.
 *
 * @param id : 식별자
 * @param parentId : 부모 식별자 (예 : Step의 경우 jobId, Task의 경우 taskId)
 * @param job, step, task : 배치 프로세스가 자신의 해당한 값을 갖는다. -> BatchProcessType를 구글 시트에서 보기 편하도록 나눔!
 * @param environment : 개발환경 (예 : local)
 * @param batchProcessName : 배치 프로세스 이름 (예 : 각 배치 프로세스 클래스의 메서드 이름)
 * @param batchProcessStatus : 배치 프로세스 상태 (예 : 완료/중지)
 * @param batchProcessStatusReason : 배치 프로세스 상태 이유 (예 : 성공/예외 발생)
 * @param detailLogId : 각 배치 프로세스의 상세 로그 Id -> 배치 결과는 상세 로그 가서 확인하도록!
 * @param executionTime : 배치 프로세스 실행 시간
 * @param createdAt : log 생성 시간 (예 : 2024-07-21 15:43:10)
 */
public record ExecutionTimeLog<T>(
    UUID id,
    UUID parentId,
    String environment,
    String job,
    String step,
    String task,
    String batchProcessName,
    BatchProcessStatus batchProcessStatus,
    String batchProcessStatusReason,
    Long detailLogId,
    long executionTime,
    String createdAt
) {

    /**
     * < 정적 팩토리 패턴을 적용한 이유 >
     * - 정적 팩토리 패턴을 적용한 이유는 필드 수정에 따른 수정 작업을 더 간단히 하고 싶었기 때문이다.
     * - 수정 하는 방법에는 크게 2가지를 생각했다.
     * 방법 1. 추가로 정적 팩토리 패턴을 활용하지 않고, ExecutionTimeLog 사용 중인 곳을 찾아서 수정된 필드들을 수정하는 방법
     * 방법 2. 정잭 팩토리 패턴을 활용하여, ExecutionTimeLog 사용 중인 곳을 찾아서, 기존에 생성자 방식을 정적 팩토리 패턴 적용 방식으로 변경하는 방법
     *
     * - 방법 1로 수정할 경우, Job, Step, Task 각각에 따라 하나하나 상황에 맞게 바꿔줘야 했고, 오타로 BatchProcessType 외의 값이 들어갈 수 있는 가능성이 있다.
     * - 반면, 방법 2는 기존 생성자의 선언 순서는 그대로 하되, 일괄적으로 new ExecutionTimeLog 를 ExecutionTimeLog.of로 바꿔주기만 하면 된다.
     * - 또한, BatchProcessType 외의 값이 들어갈 오류도 없어, 방법 1 보다 안정적으로 수정할 수 있다는 점에서 방법 2를 택했다.
     */
    public static ExecutionTimeLog of(
        UUID id,
        UUID parentId,
        String environment,
        BatchProcessType batchProcessType,
        String batchProcessName,
        BatchProcessStatus batchProcessStatus,
        String batchProcessStatusReason,
        Long detailLogId,
        long executionTime,
        String createdAt
    ) {
        if(batchProcessType.equals(BatchProcessType.JOB)) {
            return new ExecutionTimeLog(
                id,
                parentId,
                environment,
                batchProcessType.name(),
                "",
                "",
                batchProcessName,
                batchProcessStatus,
                batchProcessStatusReason,
                detailLogId,
                executionTime,
                createdAt
            );
        } else if(batchProcessType.equals(BatchProcessType.STEP)) {
            return new ExecutionTimeLog(
                id,
                parentId,
                environment,
                "",
                batchProcessType.name(),
                "",
                batchProcessName,
                batchProcessStatus,
                batchProcessStatusReason,
                detailLogId,
                executionTime,
                createdAt
            );
        } else if(batchProcessType.equals(BatchProcessType.TASK)) {
            return new ExecutionTimeLog(
                id,
                parentId,
                environment,
                "",
                "",
                batchProcessType.name(),
                batchProcessName,
                batchProcessStatus,
                batchProcessStatusReason,
                detailLogId,
                executionTime,
                createdAt
            );
        } else {
            throw new RuntimeException("알 수 없는 ~");
        }
    }
}
