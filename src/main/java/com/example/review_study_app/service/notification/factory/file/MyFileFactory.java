package com.example.review_study_app.service.notification.factory.file;

public interface MyFileFactory<T> {

    String getFileExtension();

    byte[] createFileData(T fileData);
}
