package com.example.review_study_app.service.notification.factory.file;

import com.example.review_study_app.common.enums.FileType;
import com.example.review_study_app.service.notification.vo.UnSavedLogFile;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

@Component
public class UnSavedLogFileFactory<T> {

    private final MyFileFactory myFileFactory;

    public UnSavedLogFileFactory(MyFileFactory myFileFactory) {
        this.myFileFactory = myFileFactory;
    }

    public String createFileNameWithExtension(String fileNameWithoutExtension) {
        return fileNameWithoutExtension+"."+myFileFactory.getFileExtension();
    }

    public List<ByteArrayResource> generateByteArrayResourcesFromUnSavedLogFile(List<UnSavedLogFile> files) {
        return files.stream()
            .map(file -> createByteArrayResourceFromFileData(file.fileName(), (T) file.fileData()))
            .toList();
    }

    private ByteArrayResource createByteArrayResourceFromFileData(String fileName, T fileData) {

        validateFileName(fileName);

        byte[] bytes = createFileData(fileData);

        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    private byte[] createFileData(T fileData) {
        return myFileFactory.createFileData(fileData); // TODO : 예외 처리 어떻게? 내부에서 createFileDataException 감싸서 던져주기?
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
