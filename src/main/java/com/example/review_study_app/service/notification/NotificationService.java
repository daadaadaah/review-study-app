package com.example.review_study_app.service.notification;

import com.example.review_study_app.service.notification.dto.UnSavedLogFile;
import java.util.List;

public interface NotificationService { // TODO : 도메인별로 messageFactory 만들어서 리팩토링하면 좋을 것 같음. NotificationService에 지금 너무 책임이 많음

    boolean sendMessage(String message);

    <T> boolean sendMessageWithFiles(String message, List<UnSavedLogFile> unSavedLogFiles);

    /** 로그 **/
    String createSchedulerLoggingMessage(String methodName, String startTime, String endTime, long totalExecutionTime);

    String createExecutionTimeMessage(String methodName, long totalExecutionTime);
}
