package com.example.review_study_app.common.file;

public interface TxtGenerator {

    String createTxtFileNameWithExtension(String fileNameWithoutExtension);

    byte[] generateTxt(Object data);
}
