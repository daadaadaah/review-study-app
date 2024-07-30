package com.example.review_study_app.common.service.log;

import com.example.review_study_app.common.service.log.exception.SaveLogFailException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;

/**
 * LogGoogleSheetsRepository 는 로그 데이터를 GoogleSheets에 저장하는 클래스이다.
 * 구글 시트 링크 : https://docs.google.com/spreadsheets/d/1FI3mykwcAC8pHQTpENOGmPtXBMOgofUTIOPCdEZIVV8/edit?gid=450115252#gid=450115252
 *
 * 참고
 * 0. 공식 문서 : https://developers.google.com/sheets/api/reference/rest?hl=ko
 * 1. 구글 설정 : https://many.tistory.com/11
 * 2. 코드 작성 : https://many.tistory.com/12, https://minuk22.tistory.com/89, https://www.youtube.com/watch?v=8yJrQk9ShPg
 */
@Slf4j
@Repository
public class LogGoogleSheetsRepository { // TODO : LogRepository 인터페이스 고려해보기 -> 테이블 명 어떻게?

    @Value("${google.spreadsheet.id}")
    private String SPREAD_SHEET_ID;

    private final String CREDENTIALS_FILE_PATH = "googlesheet/local-review-study-app-429913-1ec7b3c57791.json"; // local에서만 사용하는 파일이라 Github에 없음!

    private final String APPLICATION_NAME = "google-sheet-project";

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private final Environment env;

    @Autowired
    public LogGoogleSheetsRepository(Environment env) {
        this.env = env;
    }

    /**
     *
     *
     * < 주의 사항 >
     * 1. logData 내부에 필드로 커스텀 객체가 없도록 해야 한다. 객체가 있으면, 추가로 평탄화 작업이 필요하므로, 로직이 복잡해진다.
     * 2. logData의 타입이 곧 구글 시트 탭 이름이 될 수 있도록 하고, 필드의 선언 순서도 저장되는 순서임을 기억해야 한다. 또한,, XXXLog 형태로 클래스를 만들것!
     */
    public <T> void save(T logData) throws SaveLogFailException { // TODO : 추후에 비동기로 보내도 괜찮을 것 같음 +
        try {
            String logDataClassName = logData.getClass().getSimpleName();

            String range = logDataClassName+"!A5"; // 예: sheet1!A1:C4 -> Sheet1의 A1부터 C3까지

            String[] objects = convertObjectToArray(logData);

            ValueRange data = new ValueRange().setValues(Arrays.asList(
                Arrays.asList(objects)
            ));

            Sheets sheets = createSheets();

            sheets.spreadsheets().values()
                .append(SPREAD_SHEET_ID, range, data)
                .setValueInputOption("USER_ENTERED") // 참고 : https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption?hl=ko
                .setIncludeValuesInResponse(true)
                .execute();

        } catch (Exception exception) {
            log.error("google sheets 에 로그 저장 실패했습니다. exception={}, log={}", exception.getMessage(), logData);

            throw new SaveLogFailException(exception, logData.toString());
        }
    }

    private <T> String[] convertObjectToArray(T obj) throws Exception {
        try {
            Class<?> clazz = obj.getClass(); // 객체의 클래스 정보를 얻음

            Field[] fields = clazz.getDeclaredFields(); // 클래스의 모든 필드 정보를 얻음

            String[] values = new String[fields.length]; // 필드 값을 저장할 배열을 생성

            for (int i = 0; i < fields.length; i++) { // 각 필드의 값을 배열에 저장
                fields[i].setAccessible(true); // private 필드 접근 허용
                values[i] = String.valueOf(fields[i].get(obj));
            }

            return values;
        } catch (IllegalAccessException illegalAccessException) {
            log.error("접근이 불가능한 필드에 접근 시도로 Object -> Array 변환 실패했습니다. exception={}", illegalAccessException.getMessage());

            throw illegalAccessException;
        } catch (Exception exception) {
            log.error("예상치 못한 예외로 Object -> Array 변환 실패했습니다. exception={}", exception.getMessage());

            throw exception;
        }
    }

    private Credential getCredentials() throws IOException {
        if(env.acceptsProfiles("local")) {
            return getCredentialsFromJsonFile();
        } else if(env.acceptsProfiles("prod")) {
            return getCredentialsFromEnvironmentVariable();
        } else {
            String activeProfiles = String.join(",", env.getActiveProfiles());

            throw new RuntimeException("지원 하는 환경 프로파일(예 : local, prod)이 아닙니다. env="+activeProfiles); // TODO : 꼭 커스텀 예외 클래스로 만들어야 하나?
        }
    }

    private Credential getCredentialsFromJsonFile() throws IOException {
        ClassLoader loader = LogGoogleSheetsRepository.class.getClassLoader();

        FileInputStream fileInputStream = new FileInputStream(loader.getResource(CREDENTIALS_FILE_PATH).getFile());

        String fileContent = new BufferedReader(new InputStreamReader(fileInputStream)).lines().collect(Collectors.joining("\n"));

        log.info("-------------------------- Loaded credentials file content: \n{}", fileContent);

        fileInputStream = new FileInputStream(loader.getResource(CREDENTIALS_FILE_PATH).getFile()); // 로그 확인용으로 스트림 한번 소비 했으니, 다시 생성

        if (fileInputStream == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH); // TODO
        }

        return GoogleCredential.fromStream(fileInputStream).createScoped(SCOPES);
    }

    private Credential getCredentialsFromEnvironmentVariable() throws IOException {
        String projectId = System.getenv("GOOGLE_SPREADSHEET_PROJECT_ID");
        String privateKeyId = System.getenv("GOOGLE_SPREADSHEET_PRIVATE_KEY_ID");
        String beforePrivateKey = System.getenv("GOOGLE_SPREADSHEET_PRIVATE_KEY"); // TODO : 파싱 문제
        log.info("--------------------------beforePrivateKey={}", beforePrivateKey);

        String privateKey = formatPrivateKey(beforePrivateKey); // TODO : 파싱 문제
        log.info("--------------------------privateKey={}", privateKey);

        String clientEmail = System.getenv("GOOGLE_SPREADSHEET_CLIENT_EMAIL");
        String clientId = System.getenv("GOOGLE_SPREADSHEET_CLIENT_ID");

        String credentialsJson = String.format(
            "{ \"type\": \"service_account\", \n" +
                "\"project_id\": \"%s\", \n" +
                "\"private_key_id\": \"%s\", \n" +
                "\"private_key\": \"%s\", \n" +
                "\"client_email\": \"%s\", \n" +
                "\"client_id\": \"%s\", \n" +
                "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\", \n" +
                "\"token_uri\": \"https://oauth2.googleapis.com/token\", \n" +
                "\"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\", \n" +
                "\"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/google-spread-sheet%%40%s.iam.gserviceaccount.com\", \n" +
                "\"universe_domain\": \"googleapis.com\" \n"
                + " }",
            projectId, privateKeyId, privateKey, clientEmail, clientId, projectId);

        log.info("--------------------------credentialsJson={}", credentialsJson);

        InputStream credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));

        String fileContent = new BufferedReader(new InputStreamReader(credentialsStream)).lines().collect(Collectors.joining("\n"));

        log.info("-------------------------- Loaded credentials file content: \n{}", fileContent);

        credentialsStream = new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8)); // 로그 확인용으로 스트림 한번 소비 했으니, 다시 생성

        GoogleCredential credential = GoogleCredential.fromStream(credentialsStream).createScoped(SCOPES);

        return credential;
    }

    private String formatPrivateKey(String privateKey) {
        // Split the private key into parts
        String begin = "-----BEGIN PRIVATE KEY-----";
        String end = "-----END PRIVATE KEY-----";

        // Remove the begin and end parts
        String keyBody = privateKey.replace(begin, "").replace(end, "").trim();

        // Add new lines to the key body
        keyBody = keyBody.replace(" ", "\n");

        // Reconstruct the private key with new lines
        return begin + "\n" + keyBody + "\n" + end + "\n";
    }

    private Sheets createSheets() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
            .setApplicationName(APPLICATION_NAME)
            .build();
    }
}
