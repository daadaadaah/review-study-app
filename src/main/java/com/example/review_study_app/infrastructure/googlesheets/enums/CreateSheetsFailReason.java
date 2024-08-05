package com.example.review_study_app.infrastructure.googlesheets.enums;

/**
 * CreateSheetsFailReason 는 구글 시트 객체 생성 실패시, 실패 원인에 대해 카테고리화 한 enum 클래스이다.
 */
public enum CreateSheetsFailReason {
    GOOGLE_CREDENTIAL_FILE_NOT_FOUND, // 오류나는 path를 메시지가 포함하고 있음
    UNSUPPORTED_PROFILE, // 오류가 나는 profile을 메시지가 포함하고 있음
    ILLEGAL_ARGUMENT, // 잘못된 인자를 메시지가 포함하고 있음
    GOOGLE_HTTP_TRANSPORT_GENERAL_SECURITY_EXCEPTION,
    GOOGLE_CREDENTIAL_IO_EXCEPTION,
    UNKNOWN
}
