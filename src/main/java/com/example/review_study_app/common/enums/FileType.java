package com.example.review_study_app.common.enums;


public enum FileType {
    JSON("json"),
    XLSX("xlsx");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
