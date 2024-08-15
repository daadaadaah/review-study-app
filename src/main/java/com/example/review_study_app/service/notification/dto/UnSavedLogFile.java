package com.example.review_study_app.service.notification.dto;

/**
 * UnSavedLogFile 는 저장 실패된 데이터를 파일로 담기 위한 클래스이다.
 *
 * @param fileName : 파일 이름 (예 : githubApiDetailLog_1723627322626.xlsx)
 * @param fileData : 파일에 담길 데이터
 * @param <T>
 */

public record UnSavedLogFile<T>(
    String fileName, // 예 : githubApiDetailLog_1723627322626.xlsx
    T fileData
) {

}
