package com.example.review_study_app.common.file;

import com.example.review_study_app.common.enums.FileType;
import org.springframework.stereotype.Component;

/**
 * TxtBasicGenerator 은 간단하게 txt 파일을 생성하는 클래스이다.
 */

@Component
public class TxtBasicGenerator implements TxtGenerator {

    @Override
    public String createTxtFileNameWithExtension(String fileNameWithoutExtension) {
        return fileNameWithoutExtension+"."+ FileType.TXT.getExtension();
    }

    @Override
    public byte[] generateTxt(Object fileData) {
        return fileData.toString().getBytes();
    }
}
