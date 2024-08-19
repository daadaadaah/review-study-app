package com.example.review_study_app.service.notification.factory.file;


public interface JsonGenerator {

    boolean isJsonStructurePossible(String value) throws Exception;

    String createJsonFileNameWithExtension(String fileNameWithoutExtension);

    byte[] generateJSON(Object data);
}
