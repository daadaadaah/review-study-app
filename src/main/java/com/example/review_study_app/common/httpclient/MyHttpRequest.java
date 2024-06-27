package com.example.review_study_app.common.httpclient;

// 일단 당장의 필요한 필드만!
public record MyHttpRequest(
    String url,
    String message
) {

}
