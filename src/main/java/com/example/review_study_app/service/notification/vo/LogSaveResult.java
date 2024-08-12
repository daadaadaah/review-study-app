package com.example.review_study_app.service.notification.vo;

import com.example.review_study_app.service.notification.enums.LogSaveResultType;
import java.util.List;

public record LogSaveResult(

    LogSaveResultType type,
//    Throwable exception,
    String message,
    List<JsonFile> unsavedLogJsonResources
) {
}
