package com.example.review_study_app.common.file;


import com.example.review_study_app.common.enums.FileType;

public interface JsonGenerator {

    String FILE_EXTENSION = FileType.JSON.getExtension();

    boolean isJsonStructurePossible(String value) throws Exception;

    String createJsonFileNameWithExtension(String fileNameWithoutExtension);

    byte[] generateJSON(Object data);
}
