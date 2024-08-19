package com.example.review_study_app.service.notification.factory.file;


public interface JsonGenerator {

    boolean isJsonStructurePossible(String value) throws Exception;

    byte[] generateJSON(Object data);
}
