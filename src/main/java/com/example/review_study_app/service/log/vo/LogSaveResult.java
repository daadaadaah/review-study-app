package com.example.review_study_app.service.log.vo;

import com.example.review_study_app.service.notification.vo.UnSavedLogFile;
import java.util.List;

public record LogSaveResult(
    String message,
    List<UnSavedLogFile> unSavedLogFiles
) {

}
