package com.example.review_study_app.infrastructure.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleSheetsConfig {

    /**
     * < Google Sheets 객체를 Bean으로 등록한 이유 >
     *
     * 1. 조기(런타임 -> 앱 시작) 문제 발견 및 해결로 애플리케이션 안정성 향상
     *
     * [상황]
     * - Google Sheets 객체는 Google Credentials 객체를 통해 생성된다.
     * - Google Credentials 객체는 개발 환경에 따라 다르게 생성된다. (local: JSON 파일, production: 환경 변수).
     * - 이로 인해 Google Credentials 객체 생성 시 다양한 실패 원인이 존재할 수 있습니다. (예 : 잘못된 JSON 파일 경로, 구조, 환경 변수 값 등).
     *
     * [AS-IS]
     * - 기존 방식에서는 Google Sheets 객체가 사용될 때 생성되므로, 문제 발생 시 런타임에만 발견된다.
     * - 이로 인해, 코드 수정 후 Google SheetsClient 를 반복적으로 호출하여 문제를 해결해야 하는 번거로움이 있다.
     *
     * [TO-BE]
     * - Google Sheets 객체를 Bean으로 등록하면 애플리케이션 시작 시점에 초기화가 수행되며, 문제를 조기에 발견할 수 있다.
     * - Bean 초기화 과정에서 문제가 발견되면 애플리케이션이 실행되지 않으며, 이를 통해 런타임 오류를 줄일 수 있다.
     * - 또한, 객체 생성과 사용이 분리되므로 유지보수와 디버깅이 용이해진다.
     *
     * 2. 객체 재사용으로 리소스 낭비 감소
     *
     * [AS-IS]
     * - 기존 방식에서는 Google Sheets 객체가 매 호출 시마다 새로 생성되며, 이로 인해 메모리와 리소스가 낭비된다.
     * - 여러 개의 Google Sheets 객체와 Google Credentials 객체가 생성되며, OOM(Out Of Memory) 발생 가능성이 있다.
     *
     * [TO-BE]
     * - Google Sheets 객체를 Bean으로 등록하여 싱글톤으로 관리함으로써 재사용된다.
     * - API 호출의 오버헤드를 줄이고, 메모리와 리소스가 절약된다.
     * - Google Credentials 객체도 단일 인스턴스만 생성되어 리소스 낭비를 줄인다.
     *
     * 3. 객체 생성과 사용의 디커플링으로 유지보수 및 디버깅 용이
     *
     * [AS-IS]
     * - 기존 방식에서는 런타임 시 객체 생성 문제가 발생할 경우 예외 처리 로직이 필요했다.
     * - 이러한 예외 처리 로직으로 인해 코드가 복잡해지고, 디버깅 과정에서 어려움이 있었다.
     *
     * [TO-BE]
     * - 객체를 Bean으로 등록하여 애플리케이션 시작 시점에 객체 생성과 초기화가 수행되므로, 런타임 시점에는 객체 생성 문제에 신경 쓸 필요가 없다.
     * - 애플리케이션 시작 시점에서 모든 객체가 초기화되기 때문에, 초기화 과정에서 문제를 조기에 발견할 수 있다.
     * - 객체 생성과 사용의 분리로 인해 유지보수와 디버깅이 용이해지고, 코드가 더 깔끔해지며 가독성이 좋아진다.
     * - 런타임 시점에는 객체 생성 문제를 걱정할 필요가 없어지므로, 예외 처리 로직을 제거할 수 있다.
     *
     */
    @Bean
    public Sheets sheets(GoogleSheetsFactory googleSheetsFactory) {
        return googleSheetsFactory.createSheets();
    }
}
