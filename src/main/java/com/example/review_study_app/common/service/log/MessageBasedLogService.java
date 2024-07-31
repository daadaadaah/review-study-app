package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.service.log.entity.ExecutionTimeLog;
import com.example.review_study_app.common.service.event.MessageProducer;
import com.example.review_study_app.common.service.log.entity.GithubApiLog;
import com.example.review_study_app.common.service.log.entity.JobDetailLog;
import com.example.review_study_app.common.service.log.entity.StepDetailLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageBasedLogService {

    private final MessageProducer messageProducer;

    @Autowired
    public MessageBasedLogService(
        MessageProducer messageProducer
    ) {
        this.messageProducer = messageProducer;
    }

    public <T> void save(T detailLog, ExecutionTimeLog executionTimeLog) {

        messageProducer.sendMessage(detailLog, executionTimeLog);
    }

    public void saveJobLog(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog) {

    }

    public void saveStepLog(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog) {

    }

    public void saveTaskLog(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {

    }
}
