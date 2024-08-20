package com.example.review_study_app.common.file;

import com.example.review_study_app.common.enums.FileType;

public interface TxtGenerator {

    String FILE_EXTENSION = FileType.TXT.getExtension();

    String createTxtFileNameWithExtension(String fileNameWithoutExtension);

    byte[] generateTxt(Object data);
}
