# review-study-app
## 🙇‍♀️ 소개
- [주간 회고 스터디](https://github.com/daadaadaah/reviewStudy/issues)용 주간 회고 이슈를 매주 자동으로 생성 및 관리하여 수작업 대비 n배 효율을 제공하는 스프링 스케줄러 서버

```bash
## 1. 수작업 대비 n배 효율 향상 -> 계산할 떄, GPT 도움 받기
https://chatgpt.com/c/333d6971-56bf-4535-8d6a-cbdb8b324942
> 이 배치 프로그램을 만듬으로써 수작업 대비 얼마나 향상되었는지
```

### 🎈주요 기능
<img width="541" alt="스크린샷 2024-08-22 오후 3 26 55" src="https://github.com/user-attachments/assets/94dc47c1-b9ac-4df5-bbf0-0a51359b3bf6">

#### 1. ⏰ 스케줄러 기능
- Spring의 @Scheduled 애너테이션과 GitHub API를 활용하여 주기적인 작업을 자동으로 수행한다. 
- 예를 들어, GitHub 리포지토리에 Issue를 생성하거나 Close 하는 작업에 사용된다.

#### 2. 🔔 알림 기능 
- Discord Webhook API를 활용하여 로깅 또는 배치 작업의 성공/실패 여부 등을 Discord 채널에 알림을 전송한다.
- 예를 들어, 로깅 또는 배치 작업의 성공/실패 여부을 알리는 데 사용된다.

#### 3. 📝 로깅 기능
- Google Sheets API를 활용하여 GitHub API 통신 및 작업별 수행 시간 등을 Google 스프레드시트에 저장한다.
- (이를 통해 로그를 클라우드에 안전하게 저장하고, 손쉽게 조회 및 분석할 수 있습니다.)


## 🗓️ 기간
- 2024년 6월 ~

## 🛠️ 기술
- Java, Spring Boot, Gihtub API, Google API, Discord API

## 🚀 주요 성과 또는 문제 해결/개선 사례
### 1. 비동기 및 싱글 스레드를 활용하여 비즈니스 로직과 부가 기능 로직 최적화(장애 전파 문제 해결, 동시성 문제 해결 및 성능 최적화)([관련 링크](https://github.com/daadaadaah/review-study-app/issues/80))

### 2. 알림 메시지 전송 방식 및 알림 전송 주기 변경으로 디스코드 알림 기능 개선
#### 개선점 2-1. 데이터 전송 방식 변경(텍스트 기반 방식 -> 파일 첨부 방식)으로 최대 글자수 초과 문제 해결 ([관련 링크](https://github.com/daadaadaah/review-study-app/issues/79))

#### 개선점 2-2. 데이터 전송 주기 변경(모든 데이터 저장시마다 -> 한번에 배치로)으로 DX 향상([관련 링크](https://github.com/daadaadaah/review-study-app/issues/78))

### 3. 기타 개선점들 (총 n개)
#### (1) SRP 원칙을 활용하여 순환 참조 문제 해결([관련 링크](https://github.com/daadaadaah/review-study-app/issues/73))
- SRP에 기반하여 Config 클래스 분리와 Bean 분리로 순환 참조 이슈 해결

#### (2) 구글 시트 객체을 Bean으로 등록하여 조기 문제 밸견 및 해결로 애플리케이션 안정성 향상과 메모리와 리소스 낭비 감소 ([관련 링크](https://github.com/daadaadaah/review-study-app/blob/23d8bd5e1929f31f9c85583f57cf1fbede2c219d/src/main/java/com/example/review_study_app/infrastructure/googlesheets/config/GoogleSheetsConfig.java#L12)
- 팩토리 패턴과 Bean 등록으로 Google Sheets 객체 생성과 사용 디커플링시킴으로써 애플리케이션의 안정성 향상시켰고, Google Sheets 객체를 사용함으로써 메모리와 리소스 낭비 감소 시킴

#### (3) AOP 활용하여 00 향상
-  커스텀 AOP, RetryListener를 활용하여 어떤 클래스의 어떤 메서드가 몇번 재시도하는지에 대한 로깅 구현 
- @Retryable 애노테이션이 적용된 메서드 호출 전, 클래스명과 메서드명을 RetryContext에 저장하는 AOP 컴포넌트 개발 -> AOP를 활용하여 재시도 메커니즘의 투명성과 유지보수성 향상.

#### (4) AOP 대신 Interceptor 활용하여 HTTP 요청/응답 로깅 기능 구현

## 🍎 주요 클래스 구조(?) ([링크](https://app.diagrams.net/#G1G6XFKNdc9-xQOxY04WzSRmKNUTCcTDhS#%7B%22pageId%22%3A%22Ko9q1aU8WVFJBuZZ_9kq%22%7D))
<img width="1446" alt="스크린샷 2024-08-22 오후 3 35 12" src="https://github.com/user-attachments/assets/964d458a-9e60-4deb-9f46-ceb8c9e3de0a">

## 시스템 설계 및 로직
- [draw.io](https://app.diagrams.net/#G1G6XFKNdc9-xQOxY04WzSRmKNUTCcTDhS#%7B%22pageId%22%3A%222YBvvXClWsGukQMizWep%22%7D)
