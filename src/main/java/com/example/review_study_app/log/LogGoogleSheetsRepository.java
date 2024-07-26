package com.example.review_study_app.log;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final String CREDENTIALS_FILE_PATH = "googlesheet/review-study-app-429913-1ec7b3c57791.json";

    private final String APPLICATION_NAME = "google-sheet-project";

    private final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private Credential getCredentials() throws IOException {
        ClassLoader loader = LogGoogleSheetsRepository.class.getClassLoader();

        FileInputStream fileInputStream = new FileInputStream(loader.getResource(CREDENTIALS_FILE_PATH).getFile());

        GoogleCredential credential = GoogleCredential.fromStream(fileInputStream).createScoped(SCOPES);

        return credential;
    }

    private Sheets createSheets() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
            .setApplicationName(APPLICATION_NAME)
            .build();
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
            log.error("google sheets 에 로그 저장 실패했습니다. log={}", logData);

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
}
