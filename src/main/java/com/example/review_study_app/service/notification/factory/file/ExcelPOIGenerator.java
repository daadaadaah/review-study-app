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
 * ExcelPOIGenerator 은 POI 를 이용해서 Excel 파일을 생성하는 클래스이다.
 */

@Component
public class ExcelPOIGenerator implements ExcelGenerator {

    // 참고 : https://support.microsoft.com/en-us/office/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
    private static final int POI_MAX_CELL_TEXT_LENGTH = 32767;

    @Override
    public boolean isExcelValueLengthOverLimit(Object value) {
        return value.toString().length() > POI_MAX_CELL_TEXT_LENGTH;
    }

    @Override
    public String createExcelFileNameWithExtension(String fileNameWithoutExtension) {
        return fileNameWithoutExtension+"."+ FileType.XLSX.getExtension();
    }

    @Override
    public byte[] createExcel(String sheetName, Object fileData)  {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet(sheetName);

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

            String className = fileData.getClass().getSimpleName();
            String fieldName = fields[i].getName();
            Object value = null;

            try {
                value = fields[i].get(fileData);

                if(value != null && isExcelValueLengthOverLimit(value)) { // TODO : GET 요청이고, 갯수가 많을 때, 초과할 수 있음

                    throw new RuntimeException(""); // TODO : 밑의 코드에서 예외 던져주는데, 내가 또 검증 로직을 작성해야 하나?
                }

                dataRow.createCell(i).setCellValue(value != null ? value.toString() : "");
            } catch (IllegalArgumentException e) {

                throw new RuntimeException("Excel 파일을 생성하기 위해 객체의 필드 값이 유효하지 않습니다. 필드가 접근 가능한지 확인하고 다시 시도하십시오. 객체="+className+", 필드="+fieldName +", 필드값="+value +", value.size="+value.toString().length(), e); // TODO : 커스텀 예외
            } catch (IllegalAccessException e) {

                throw new RuntimeException("Excel 파일을 생성하기 위해 객체의 필드 값을 접근하는 데 실패했습니다. 필드가 접근 가능한지 확인하고 다시 시도하십시오. 객체="+className+", 필드="+fieldName, e); // TODO : 커스텀 예외
            } catch (Exception exception) {

                throw new RuntimeException("Excel 파일 생성 실패, 원인 모름 객체="+className+", 필드="+fieldName+", 필드값="+value, exception); // TODO : 커스텀 예외
            }
        }
    }
}
