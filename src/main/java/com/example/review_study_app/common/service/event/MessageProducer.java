package com.example.review_study_app.common.service.event;

import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MessageProducer<T> {

    private final ApplicationEventPublisher eventPublisher;
    private final Queue<LogMessageEvent> messageQueue;

    @Autowired
    public MessageProducer(ApplicationEventPublisher eventPublisher, Queue detailLogMessageEventQueue) {
        this.eventPublisher = eventPublisher;
        this.messageQueue = detailLogMessageEventQueue;
    }

    public void sendMessage(T detailLog, ExecutionTimeLog executionTimeLog) {
        String threadName = Thread.currentThread().getName();

        log.info("[MessageProducer | sendMessage]--------------"+executionTimeLog.batchProcessName()+"------------Event handled by thread: " + threadName);






        LogMessageEvent logMessageEvent = new LogMessageEvent(this, detailLog, executionTimeLog);

        messageQueue.offer(logMessageEvent);

        eventPublisher.publishEvent(logMessageEvent);
    }
}