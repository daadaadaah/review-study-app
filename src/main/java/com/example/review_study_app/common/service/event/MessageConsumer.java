package com.example.review_study_app.common.service.event;

import com.example.review_study_app.common.service.log.LogGoogleSheetsRepository;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class MessageConsumer {

    private final LogGoogleSheetsRepository logGoogleSheetsRepository;

    private final Queue<LogMessageEvent> messageQueue;

    @Autowired
    public MessageConsumer(
        Queue<LogMessageEvent> detailLogMessageEventQueue,
        LogGoogleSheetsRepository logGoogleSheetsRepository
    ) {
        this.messageQueue = detailLogMessageEventQueue;
        this.logGoogleSheetsRepository = logGoogleSheetsRepository;
    }


    @Async("eventTaskExecutor")
    @EventListener
    public void handleMessageEvent(LogMessageEvent event) {

        String threadName = Thread.currentThread().getName();
        log.info("[handleMessageEvent1]-----------"+event.getExecutionTimeLog().batchProcessName()+"----------------Event handled by thread: " + threadName);

        consumeMessages();

        log.info("[handleMessageEvent2]-----------"+event.getExecutionTimeLog().batchProcessName()+"----------------Event handled by thread: " + threadName);
    }

    private void consumeMessages() {
        while (!messageQueue.isEmpty()) {

            LogMessageEvent message = messageQueue.poll();

            log.info("[MessageConsumer | consumeMessages]-------------------------"+message.getExecutionTimeLog().batchProcessName());

            if (message != null) {
                processMessage(message);
            }
        }
    }

    private void processMessage(LogMessageEvent message) {
        String threadName = Thread.currentThread().getName();

        log.info("[MessageConsumer | processMessage]--------------"+message.getExecutionTimeLog().batchProcessName()+"------------Event handled by thread: " + threadName);


        logGoogleSheetsRepository.save(message.getDetailLog());
        logGoogleSheetsRepository.save(message.getExecutionTimeLog());
    }
}