package com.example.review_study_app.service.notification.factory.file;

import com.example.review_study_app.common.enums.FileType;
import com.example.review_study_app.service.notification.vo.UnSavedLogFile;
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

    public String createExcelFileNameWithExtension(String fileNameWithoutExtension) {
        return excelGenerator.createExcelFileNameWithExtension(fileNameWithoutExtension);
    }

    public String createJsonFileNameWithExtension(String fileNameWithoutExtension) {
        return jsonGenerator.createJsonFileNameWithExtension(fileNameWithoutExtension);
    }

    public String createTxtFileNameWithExtension(String fileNameWithoutExtension) {
        return txtGenerator.createTxtFileNameWithExtension(fileNameWithoutExtension);
    }

    public boolean isExcelCellValueLengthOverLimit(Object value) {
        return excelGenerator.isExcelValueLengthOverLimit(value);
    }

    public boolean isJsonStructurePossible(String value) {
        try {
            return jsonGenerator.isJsonStructurePossible(value);
        } catch (Exception exception) {
            throw new RuntimeException("isJsonStructurePossible 예외"); // TODO : 커스텀 예외
        }
    }

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
