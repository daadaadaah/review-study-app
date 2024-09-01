# review-study-app
## 🙇‍♀️ 소개
- [주간 회고 스터디](https://github.com/daadaadaah/reviewStudy/issues)용 주간 회고 이슈를 매주 자동으로 생성 및 관리하여 수작업 대비 3배(1인당 30초 -> 10초) 효율을 제공하는 스프링 스케줄러 서버

```bash
# 수작업
- 1인당 작업 시간 :  30초
Github 레포 열기: 1초
이슈 목록에서 내 Issue 클릭하기: 1초
Close 하기: 1초
다음 주차 계산하기: 3초
다음 주차 Label 생성하기: 3초
이슈 내용 기재하고 주차 Label 추가 및 생성 : 21초

# 자동화
- 총 수행 시간 : 평균 10초
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
- AOP와 Interceptor 활용하여 로깅 기능 구현


## 🗓️ 기간
- 2024년 6월 ~

## 🛠️ 기술
- Java, Spring Boot, Gihtub API, Google API, Discord API

## 🚀 주요 성과 또는 문제 해결/개선 사례
### 1. 비동기 및 싱글 스레드를 활용하여 비즈니스 로직과 부가 기능 로직 최적화
#### - 장애 전파 문제 해결, 동시성 문제 해결 및 성능 최적화 ([문제 해결 과정 링크](https://github.com/daadaadaah/review-study-app/issues/80))

### 2. 알림 메시지 전송 방식 및 알림 전송 주기 변경으로 디스코드 알림 기능 개선
#### 개선점 2-1. 데이터 전송 방식 변경(텍스트 기반 방식 -> 파일 첨부 방식)으로 최대 글자수 초과 문제 해결 ([문제 해결 과정 링크](https://github.com/daadaadaah/review-study-app/issues/79))

#### 개선점 2-2. 데이터 전송 주기 변경(모든 데이터 저장시마다 -> 한번에 배치로)으로 DX 향상([문제 해결 과정 링크](https://github.com/daadaadaah/review-study-app/issues/78))

### 3. 기타 개선점들 (총 3개)
#### (1) SRP 원칙을 활용하여 순환 참조 문제 해결([문제 해결 과정 링크](https://github.com/daadaadaah/review-study-app/issues/73))
- SRP에 기반하여 Config 클래스 분리와 Bean 분리로 순환 참조 이슈 해결

#### (2) 구글 시트 객체을 Bean으로 등록하여 조기 문제 밸견 및 해결로 애플리케이션 안정성 향상과 메모리와 리소스 낭비 감소 ([관련 링크](https://github.com/daadaadaah/review-study-app/blob/23d8bd5e1929f31f9c85583f57cf1fbede2c219d/src/main/java/com/example/review_study_app/infrastructure/googlesheets/config/GoogleSheetsConfig.java#L12))
- 팩토리 패턴과 Bean 등록으로 Google Sheets 객체 생성과 사용 디커플링시킴으로써 애플리케이션의 안정성 향상시켰고, Google Sheets 객체를 사용함으로써 메모리와 리소스 낭비 감소 시킴

#### (3) AOP 활용하여 재시도 메커니즘의 유지보수성 향상([관련 코드](https://github.com/daadaadaah/review-study-app/blob/main/src/main/java/com/example/review_study_app/common/retry/RetryableExecutionContextAspect.java))
- @Retryable 애노테이션이 적용된 메서드 호출 전, 클래스명과 메서드명을 RetryContext에 저장하는 AOP 컴포넌트 개발하여 재시도 메커니즘의 투명성과 유지보수성 향상. 

## 🍎 주요 클래스 구조 ([크게 보기](https://github.com/user-attachments/assets/e9635852-a7e3-4e85-838f-72b5b1501aa5))
<img width="1446" alt="스크린샷 2024-08-22 오후 3 35 12" src="https://github.com/user-attachments/assets/964d458a-9e60-4deb-9f46-ceb8c9e3de0a">

