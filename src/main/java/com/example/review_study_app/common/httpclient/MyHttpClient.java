package com.example.review_study_app.common.httpclient;

// HttpClient로 하려 했으나, java.net.http.HttpClient와 헷갈리지 않게 MyHttpClient 붙임
public interface MyHttpClient {

    MyHttpResponse sendRequest(String message) throws Exception;
}
