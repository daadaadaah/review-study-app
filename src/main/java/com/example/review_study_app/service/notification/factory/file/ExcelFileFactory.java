package com.example.review_study_app.service.notification.factory.file;

import com.example.review_study_app.common.enums.FileType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * ExcelFileFactory 은 Excel 파일을 생성하는 클래스이다.
 */

@Component
public class ExcelFileFactory implements MyFileFactory {

    @Override
    public String getFileExtension() {
        return FileType.XLSX.getExtension();
    }

    @Override
    public byte[] createFileData(Object fileData) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(fileData.getClass().getSimpleName());

            Field[] fields = fileData.getClass().getDeclaredFields();

            createHeaderRow(sheet, fields);

            createDataRow(sheet, fields, fileData);

            byte[] bytes = createByteArray(workbook);

            return bytes;
        } catch (IOException e) {
            throw new RuntimeException("Excel 파일을 생성하는 동안 I/O 오류가 발생했습니다. 파일을 생성하거나 시스템의 상태를 확인하고 다시 시도하십시오.", e); // TODO : 커스텀 예외
        }
    }

    private byte[] createByteArray(Workbook workbook) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            workbook.write(outputStream);

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Excel 파일을 바이트 배열로 변환하는 동안 I/O 오류가 발생했습니다. 시스템의 상태를 확인하고 다시 시도하십시오.", e); // TODO : 커스텀 예외
        }
    }

    private void createHeaderRow(Sheet sheet, Field[] fields) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < fields.length; i++) {
            headerRow.createCell(i).setCellValue(fields[i].getName());
        }
    }

    private void createDataRow(Sheet sheet, Field[] fields, Object fileData) {
        Row dataRow = sheet.createRow(1);

        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);

            try {
                Object value = fields[i].get(fileData);
                dataRow.createCell(i).setCellValue(value != null ? value.toString() : "");

            } catch (IllegalAccessException e) {
                String fieldName = fields[i].getName();
                String className = fileData.getClass().getSimpleName();

                throw new RuntimeException("Excel 파일을 생성하기 위해 객체의 필드 값을 접근하는 데 실패했습니다. 필드가 접근 가능한지 확인하고 다시 시도하십시오. 객체="+className+", 필드="+fieldName, e); // TODO : 커스텀 예외
            }
        }
    }
}
