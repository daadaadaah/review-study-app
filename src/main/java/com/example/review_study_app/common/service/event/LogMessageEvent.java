package com.example.review_study_app.common.service.event;

import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import org.springframework.context.ApplicationEvent;

public class LogMessageEvent<T> extends ApplicationEvent {
    private final T detailLog;

    private final ExecutionTimeLog executionTimeLog;


    public LogMessageEvent(Object source, T detailLog, ExecutionTimeLog executionTimeLog) {
        super(source);
        this.detailLog = detailLog;
        this.executionTimeLog = executionTimeLog;
    }

    public T getDetailLog() {
        return detailLog;
    }

    public ExecutionTimeLog getExecutionTimeLog() {
        return executionTimeLog;
    }
}
