package com.example.review_study_app.common.httpclient;

// HttpClient로 하려 했으나, java.net.http.HttpClient와 헷갈리지 않게 MyHttpClient 붙임
// 디스코드 통신 때 아직 사용하고 있어서 아직 삭제하면 안됨
public interface MyHttpClient {

    MyHttpResponse post(MyHttpRequest request) throws Exception;
}
