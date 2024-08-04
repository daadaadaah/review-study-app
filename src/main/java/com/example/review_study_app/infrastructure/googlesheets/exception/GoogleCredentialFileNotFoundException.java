package com.example.review_study_app.infrastructure.googlesheets.exception;

import java.io.FileNotFoundException;

/**
 * GoogleCredentialFileNotFoundException 는 JSON 파일 경로 Google Credentials 생성 실패시 발생하는 예외이다.
 */
public class GoogleCredentialFileNotFoundException extends FileNotFoundException {

    public GoogleCredentialFileNotFoundException(String filePath) {
        super("Google Credential 파일을 확인해주세요. filePath="+filePath);
    }
}
