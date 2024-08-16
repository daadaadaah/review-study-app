package com.example.review_study_app.service.notification;

import com.example.review_study_app.service.notification.dto.UnSavedLogFile;
import java.util.List;

public interface NotificationService {

    boolean sendMessage(String message);

    <T> boolean sendMessageWithFiles(String message, List<UnSavedLogFile> unSavedLogFiles);
}
