package com.example.review_study_app.service.log.dto;

import com.example.review_study_app.service.log.enums.LogSaveResultType;
import com.example.review_study_app.service.notification.dto.UnSavedLogFile;
import java.util.List;

public record LogSaveResult(
    LogSaveResultType type,
    String message,
    List<UnSavedLogFile> unSavedLogFiles
) {

}
