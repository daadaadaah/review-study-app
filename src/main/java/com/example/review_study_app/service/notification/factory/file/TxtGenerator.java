package com.example.review_study_app.service.notification.factory.file;

public interface TxtGenerator {

    String createTxtFileNameWithExtension(String fileNameWithoutExtension);

    byte[] generateTxt(Object data);
}
