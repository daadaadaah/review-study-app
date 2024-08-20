package com.example.review_study_app.service.notification.factory.file;

import com.example.review_study_app.common.enums.FileType;
import com.example.review_study_app.common.file.ExcelGenerator;
import com.example.review_study_app.common.file.JsonGenerator;
import com.example.review_study_app.common.file.TxtGenerator;
import com.example.review_study_app.service.notification.vo.FieldToChange;
import com.example.review_study_app.service.notification.vo.UnSavedLogFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnSavedLogFileFactory<T> {

    private final ExcelGenerator excelGenerator;

    private final JsonGenerator jsonGenerator;

    private final TxtGenerator txtGenerator;

    public UnSavedLogFileFactory(
        ExcelGenerator excelGenerator,
        JsonGenerator jsonGenerator,
        TxtGenerator txtGenerator
    ) {
        this.excelGenerator = excelGenerator;
        this.jsonGenerator = jsonGenerator;
        this.txtGenerator = txtGenerator;
    }

    /** UnSavedLogFileFactory 의 역할 1. 확장자를 포함한 파일이름 생성 **/
    public String createUnSavedLogFileNameWithExtension(String fileNameWithoutExtension) {
        return excelGenerator.createExcelFileNameWithExtension(fileNameWithoutExtension);
    }

    /** UnSavedLogFileFactory 의 역할 2. 액셀 최대 셀 텍스트 크기에 맞게 UnSavedLogFile 변환 **/
    public  <T> List<UnSavedLogFile> transformUnSavedLogFileByExcelMaxCellTextSize(List<UnSavedLogFile> unSavedLogFiles) {
        List<UnSavedLogFile> newUnSavedLogFiles = new ArrayList<>();

        List<FieldToChange> fieldToChanges = new ArrayList<>();

        try {
            for(UnSavedLogFile unSavedLogFile: unSavedLogFiles) {

                T logData = (T) unSavedLogFile.fileData();

                Field[] fields = unSavedLogFile.fileData().getClass().getDeclaredFields();

                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];

                    field.setAccessible(true);

                    String fieldName = field.getName();

                    Object fieldValue = field.get(logData);

                    if(validateUnSavedLog(fieldValue)) {

                        if (isJsonStructurePossible(fieldValue.toString())) {
                            // JSON 파일
                            String jsonFileName = createFileNameWithExtensionForObjectOrArray( fieldName + "_" + getFieldValue(logData, "id"));

                            // 해당 필드값을 jsonFileName으로 변경
                            newUnSavedLogFiles.add(new UnSavedLogFile(jsonFileName, fieldValue));

                            fieldToChanges.add(new FieldToChange(fieldName, jsonFileName));

                        } else {
                            // txt 파일
                            String txtFileName = createFileNameWithExtensionForNonObjectOrArray( fieldName + "_" + getFieldValue(logData, "id"));;

                            newUnSavedLogFiles.add(new UnSavedLogFile(txtFileName, fieldValue));

                            fieldToChanges.add(new FieldToChange(fieldName, txtFileName));
                        }
                    }
                }

                String logFileName = unSavedLogFile.fileName();

                if(fieldToChanges.size() > 0) {
                    T newLogData = createNewInstanceWithModifiedField(logData, fieldToChanges);

                    newUnSavedLogFiles.add(new UnSavedLogFile(logFileName, newLogData));

                } else {
                    newUnSavedLogFiles.add(new UnSavedLogFile(logFileName, logData));
                }
            }
        } catch (Exception exception) {
            throw new RuntimeException("createUnSavedLogFiles 에외 발생="+exception.getMessage()); // TODO : 예외 처리 더 고민해보기
        }

        return newUnSavedLogFiles;
    }

    private String createFileNameWithExtensionForObjectOrArray(String fileNameWithoutExtension) {
        return jsonGenerator.createJsonFileNameWithExtension(fileNameWithoutExtension);
    }

    private String createFileNameWithExtensionForNonObjectOrArray(String fileNameWithoutExtension) {
        return txtGenerator.createTxtFileNameWithExtension(fileNameWithoutExtension);
    }

    private boolean validateUnSavedLog(Object value) {
        return excelGenerator.isExcelValueLengthOverLimit(value);
    }

    private boolean isJsonStructurePossible(String value) {
        try {
            return jsonGenerator.isJsonStructurePossible(value);
        } catch (Exception exception) {
            throw new RuntimeException("isJsonStructurePossible 예외"); // TODO : 커스텀 예외
        }
    }

    private Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value", e);
        }
    }

    private <T> T createNewInstanceWithModifiedField(T originalObject, List<FieldToChange> fieldToChanges) throws Exception {
        if(fieldToChanges == null || fieldToChanges.size() == 0) {
            // TODO :
        }

        // 원래 객체의 클래스 정보를 얻습니다.
        Class<?> clazz = originalObject.getClass();

        // 필드 값을 수정하여 새로운 객체를 생성합니다.
        Field[] fields = clazz.getDeclaredFields();
        Object[] fieldValues = new Object[fields.length];

        // 모든 필드를 원본 객체로부터 가져옵니다.
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            fieldValues[i] = field.get(originalObject);
        }

        // 필드 값을 수정합니다.
        for (FieldToChange fieldToChange : fieldToChanges) {
            String fieldName = fieldToChange.fieldNameToChange();
            Object newValue = fieldToChange.dataFileName();

            boolean fieldModified = false;
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.getName().equals(fieldName)) {
                    fieldValues[i] = newValue;
                    fieldModified = true;
                    break;
                }
            }

            if (!fieldModified) {
                throw new NoSuchFieldException("Field " + fieldName + " does not exist in class " + clazz.getName());
            }
        }

        // 생성자 정보를 얻습니다.
        Constructor<?> constructor = clazz.getConstructors()[0];

        return (T) constructor.newInstance(fieldValues);
    }

    /** UnSavedLogFileFactory 의 역할 3. UnSavedLogFile 목록을 토대로 ByteArrayResources 생성 **/
    public List<ByteArrayResource> generateByteArrayResourcesFromUnSavedLogFile(List<UnSavedLogFile> files) {
        return files.stream()
            .map(file -> createByteArrayResourceFromFileData(file.fileName(), (T) file.fileData()))
            .toList();
    }

    private ByteArrayResource createByteArrayResourceFromFileData(String fileName, T fileData) {
        validateFileName(fileName);

        byte[] bytes = createFile(fileName, fileData);

        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    private byte[] createFile(String fileName, T fileData) {

        String fileExtension = extractFileExtension(fileName);

        if(fileExtension.equals(FileType.XLSX.getExtension())) {
            return excelGenerator.createExcel(fileData.getClass().getSimpleName(), fileData); // TODO : 예외 처리 어떻게? 내부에서 createFileDataException 감싸서 던져주기?
        }

        if(fileExtension.equals(FileType.JSON.getExtension())) {
            return jsonGenerator.generateJSON(fileData); // TODO : 예외 처리 어떻게? 내부에서 createFileDataException 감싸서 던져주기?
        }

        if(fileExtension.equals(FileType.TXT.getExtension())) {
            return txtGenerator.generateTxt(fileData); // TODO : 예외 처리 어떻게? 내부에서 createFileDataException 감싸서 던져주기?
        }

        throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 파일을 확인해주세요. fileName=" + fileName);
    }

    private void validateFileName(String fileName) {
        String fileExtension = extractFileExtension(fileName);

        if (!isSupportedExtensions(fileExtension)) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. 파일을 확인해주세요. fileName=" + fileName);
        }
    }

    private String extractFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("파일을 확인해주세요. fileName=" + fileName);
        }

        return fileName.substring(lastDotIndex + 1);
    }

    private boolean isSupportedExtensions(String fileExtension) {
        return Arrays.stream(FileType.values())
            .map(FileType::getExtension)
            .collect(Collectors.toSet())
            .contains(fileExtension);
    }
}
