package com.example.review_study_app.common.file;


import com.example.review_study_app.common.enums.FileType;

public interface ExcelGenerator<T> {

    // 엑셀 파일을 생성할 때, 셀에 입력할 수 있는 최대 글자 수는 사용하는 라이브러리와 무관하게 엑셀 파일 형식 자체의 제한에 따른다.
    // 수치 참고 : https://support.microsoft.com/en-us/office/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
    int MAX_CELL_TEXT_LENGTH = 32767;

    String FILE_EXTENSION = FileType.XLSX.getExtension();

    boolean isExcelValueLengthOverLimit(Object value);

    String createExcelFileNameWithExtension(String fileNameWithoutExtension);

    byte[] createExcel(String sheetName, T fileData);
}
