package com.example.review_study_app.service.notification.factory.file;


public interface ExcelGenerator<T> {

    boolean isExcelValueLengthOverLimit(Object value);

    String createExcelFileNameWithExtension(String fileNameWithoutExtension);

    byte[] createExcel(String sheetName, T fileData);
}
