package com.example.review_study_app.infrastructure.googlesheets.factory;

import static com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason.GOOGLE_HTTP_TRANSPORT_GENERAL_SECURITY_EXCEPTION;
import static com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason.GOOGLE_CREDENTIAL_FILE_NOT_FOUND;
import static com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason.GOOGLE_CREDENTIAL_IO_EXCEPTION;
import static com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason.ILLEGAL_ARGUMENT;
import static com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason.UNKNOWN;
import static com.example.review_study_app.infrastructure.googlesheets.enums.CreateSheetsFailReason.UNSUPPORTED_PROFILE;

import com.example.review_study_app.infrastructure.googlesheets.exception.GoogleCredentialFileNotFoundException;
import com.example.review_study_app.repository.log.LogGoogleSheetsRepository;
import com.example.review_study_app.infrastructure.googlesheets.exception.CreateSheetsFailException;
import com.example.review_study_app.infrastructure.googlesheets.exception.UnsupportedProfileException;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * GoogleSheetsFactory 는 구글 시트 객체 생성 책임이 있는 팩토리 클래스이다.
 *
 * 이 클래스를 만든 이유는 GoogleSheets 객체 생성과 관련되어서 많은 로직이 있어서, 객체 생성과 객체 사용 분리하는게 좋을 것 같아서, 분리했다.
 * 특히, 예외 발생이 객체 생성에서 발생했는지, 객체 사용시에 발생했는지를 분기해서 생각할 수 있어서 좋은 것 같다.
 */
@Slf4j
@Component
public class GoogleSheetsFactory {

    private final String CREDENTIALS_FILE_PATH = "googlesheet/local-review-study-app-429913-1ec7b3c57791.json"; // local에서만 사용하는 파일이라 Github에 없음!

    private final String APPLICATION_NAME = "google-sheet-project";

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private final Environment env;

    @Autowired
    public GoogleSheetsFactory(Environment env) {
        this.env = env;
    }

    public Sheets createSheets() { // TODO : sheets 를 만들기 위한 하위 단계의 모든 예외는 다 여기서 처리되도록 해놨는데, 피드백 받아야 함.
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport(); // GeneralSecurityException 발생

            Credential credential = createCredentials(); // IOException 발생

            return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        } catch (GoogleCredentialFileNotFoundException exception) {
            throw new CreateSheetsFailException(GOOGLE_CREDENTIAL_FILE_NOT_FOUND, exception);

        } catch (UnsupportedProfileException exception) {
            throw new CreateSheetsFailException(UNSUPPORTED_PROFILE, exception);

        } catch (IllegalArgumentException exception) {
            throw new CreateSheetsFailException(ILLEGAL_ARGUMENT, exception);

        } catch (GeneralSecurityException exception) {
            throw new CreateSheetsFailException(GOOGLE_HTTP_TRANSPORT_GENERAL_SECURITY_EXCEPTION, exception);

        } catch (IOException exception) {
            throw new CreateSheetsFailException(GOOGLE_CREDENTIAL_IO_EXCEPTION, exception);

        } catch (Exception exception) {
            throw new CreateSheetsFailException(UNKNOWN, exception);
        }
    }

    /**
     * createCredentials 는 구글 계정 객체를 생성하는 메서드입니다.
     *
     * < profile 마다 다른 방식인 이유 >
     * - 개발 환경마다 다른 장단점이 있으므로,
     *
     * 1. local에서 코드에서 직접 JSON 을 생성하지 않고, JSON 파일을 사용한 이유
     * - Google Credentials을 위해 필요한 정보들은 외부로 노출되어서 안되는 민감한 정보들이다.
     * - 만약, 코드에서 직접 JSON 을 생성하게 되면, Github 에 업로드되는 문제가 있다.
     * - 반면, JSON 파일을 사용하는 경우, 그 파일만 Github에 업로드되지 않도록 하면 되고,
     * - local 에서 개발할 때, JSON 파일 내부의 값을 손쉽게 변경하면서 사용할 수 있으므로,
     * - local 에서는 JSON 파일을 사용하도록 했다.
     *
     * 2. prod에서 JSON 파일을 사용하지 않고, 코드에서 직접 JSON 을 생성해서 사용하는 이유
     * - prod 환경에서는 민감한 정보를 코드에 포함시키지 않기 위해 환경 변수를 사용한다.
     * - credientials 생성 과정과 관련해서 디버깅할 때, 환경 변수에 대한 값을 확인할 필요가 있는데,
     * - 이때, JSON 파일의 경우, 값 확인이 어렵다는 문제가 있다.
     * - 반면, 코드에서 직접 JSON을 생성하는 경우, 각각의 환경변수와 그 환경변수로 만들어진 JSON 형태를 직접 확인할 수 있어서, 디버깅이 상대적으로 용이하다.
     * - 따라서, prod에서는 코드에서 직접 JSON 을 생성해서 사용하도록 했다.
     */
    private Credential createCredentials() throws IOException {

        if (!env.acceptsProfiles("local") && !env.acceptsProfiles("prod")) {
            String activeProfiles = String.join(",", env.getActiveProfiles());

            throw new UnsupportedProfileException(activeProfiles);
        }

        if (env.acceptsProfiles("local")) {
            return createCredentialsFromJsonFile();
        }

        return createCredentialsFromEnvironmentVariable();
    }

    /**
     * createCredentialsFromJsonFile 는 JSON 파일로 Google Credentials 을 생성하는 메서드이다.
     */
    private Credential createCredentialsFromJsonFile() throws IOException {
        ClassLoader loader = LogGoogleSheetsRepository.class.getClassLoader();

        URL googleCredentialUrl = loader.getResource(CREDENTIALS_FILE_PATH);

        if (googleCredentialUrl == null) {
            throw new GoogleCredentialFileNotFoundException(CREDENTIALS_FILE_PATH);
        }

        try (FileInputStream fileInputStream = new FileInputStream(googleCredentialUrl.getFile())) {
            return GoogleCredential.fromStream(fileInputStream).createScoped(SCOPES);
        }
    }

    /**
     * createCredentialsFromEnvironmentVariable 는 환경변수을 가지고 JSON을 생성하여Google Credentials 을 생성하는 메서드이다.
     *
     * 참고 : 로그로 확인할 때, 스트림은 1회성이므로, 스트림 한번 더 만들어줘야함!
     */
    private Credential createCredentialsFromEnvironmentVariable() throws IOException {

        String projectId = System.getenv("GOOGLE_SPREADSHEET_PROJECT_ID");
        String privateKeyId = System.getenv("GOOGLE_SPREADSHEET_PRIVATE_KEY_ID");
        String privateKey = System.getenv("GOOGLE_SPREADSHEET_PRIVATE_KEY");
        String clientEmail = System.getenv("GOOGLE_SPREADSHEET_CLIENT_EMAIL");
        String clientId = System.getenv("GOOGLE_SPREADSHEET_CLIENT_ID");

        if (Arrays.asList(projectId, privateKeyId, privateKey, clientEmail, clientId).contains(null)) {
            throw new IllegalArgumentException("다음 Google Credential 환경 변수를 확인해주세요. \n"
                + "< Google Credential 환경 변수 > \n"
                + "- GOOGLE_SPREADSHEET_PROJECT_ID \n"
                + "- GOOGLE_SPREADSHEET_PRIVATE_KEY_ID \n"
                + "- GOOGLE_SPREADSHEET_PRIVATE_KEY \n"
                + "- GOOGLE_SPREADSHEET_CLIENT_EMAIL \n"
                + "- GOOGLE_SPREADSHEET_CLIENT_ID ");
        }

        String formattedPrivateKey = formatPrivateKey(privateKey); // JSON 형식에 맞게 하기 위해서 포멧팅 함.

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
            projectId, privateKeyId, formattedPrivateKey, clientEmail, clientId, projectId);

        byte[] credentialsJsonBytes = credentialsJson.getBytes(StandardCharsets.UTF_8);

        try (InputStream credentialsStream = new ByteArrayInputStream(credentialsJsonBytes)) {
            GoogleCredential credential = GoogleCredential.fromStream(credentialsStream).createScoped(SCOPES);
            return credential;
        }
    }

    /**
     * formatPrivateKey 는 의 privateKey를 google credential 형식에 맞게 포매팅해주는 함수이다.
     *
     * < 알아두면 좋은 사항 >
     * 1. "\\n"로 해주는 이유
     * - JSON 문자열 내에서 제어 문자(예: 줄바꿈 문자 \n 등)은 직접 사용할 수 없으므로, 백슬래시를 사용하여 이스케이프 처리함.
     * - 만약, 해주지 않으면, 다음과 같은 에러가 발생한다.
     * - com.fasterxml.jackson.core.JsonParseException: Illegal unquoted character ((CTRL-CHAR, code 10)): has to be escaped using backslash to be included in string value
     *
     */
    private String formatPrivateKey(String privateKey) {
        if(privateKey == null) {
            throw new IllegalArgumentException("privateKey 가 null 입니다. privateKey 를 확인해주세요.");
        }

        String begin = "-----BEGIN PRIVATE KEY-----";
        String end = "-----END PRIVATE KEY-----";

        if (!privateKey.contains(begin) || !privateKey.contains(end)) {
            throw new IllegalArgumentException("privateKey 가 유효하지 않은 형식입니다. privateKey 를 확인해주세요.");
        }

        String keyBody = privateKey.replace(begin, "").replace(end, "").trim(); // body만 뽑아내기

        keyBody = keyBody.replace(" ", "\\n"); // 공백 -> 이스케이프된 줄바꿈 변경

        return begin + "\\n" + keyBody + "\\n" + end + "\\n";
    }
}
