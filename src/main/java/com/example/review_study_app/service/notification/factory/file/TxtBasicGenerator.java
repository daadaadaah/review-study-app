package com.example.review_study_app.service.notification.factory.file;

import org.springframework.stereotype.Component;

/**
 * TxtBasicGenerator 은 간단하게 txt 파일을 생성하는 클래스이다.
 */

@Component
public class TxtBasicGenerator implements TxtGenerator {

    @Override
    public byte[] generateTxt(Object fileData) {
        return fileData.toString().getBytes();
    }
}
