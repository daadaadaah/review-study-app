package com.example.review_study_app.service.notification.factory.file;


public interface ExcelGenerator<T> {

    boolean isExcelValueLengthOverLimit(Object value);

    byte[] createExcel(String sheetName, T fileData);
}
