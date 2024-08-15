package com.example.review_study_app.service.notification.factory.file;

import com.example.review_study_app.common.enums.FileType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JsonFileFactory 은 JSON 파일을 생성하는 클래스이다.
 */

@Deprecated
public class JsonFileFactory implements MyFileFactory {

    private final ObjectMapper objectMapper;

    public JsonFileFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getFileExtension() {
        return FileType.JSON.getExtension();
    }

    @Override
    public byte[] createFileData(Object fileData) {
        try {
            return objectMapper.writeValueAsBytes(fileData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // TODO : 커스텀 예외
        }
    }
}
