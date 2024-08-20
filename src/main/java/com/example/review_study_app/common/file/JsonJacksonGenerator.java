package com.example.review_study_app.common.file;

import com.example.review_study_app.common.enums.FileType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * JsonFileFactory 은 JSON 파일을 생성하는 클래스이다.
 */

@Component
public class JsonJacksonGenerator implements JsonGenerator {

    private final ObjectMapper objectMapper;

    public JsonJacksonGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String createJsonFileNameWithExtension(String fileNameWithoutExtension) {
        return fileNameWithoutExtension+"."+ JsonGenerator.FILE_EXTENSION;
    }

    @Override
    public boolean isJsonStructurePossible(String value) throws Exception { // TODO : 예외 처리 다시 생각해봐야 함
        JsonNode jsonNode = objectMapper.readTree(value);

        return jsonNode.isObject() || jsonNode.isArray();
    }

    @Override
    public byte[] generateJSON(Object fileData) { // TODO : 예외 처리 다시 생각해봐야 함
        try {
            return objectMapper.writeValueAsBytes(fileData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (Exception exception) {
            throw exception;
        }
    }
}
