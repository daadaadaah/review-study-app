package com.example.review_study_app.repository.log;

import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.infrastructure.googlesheets.GoogleSheetsClient;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * LogGoogleSheetsRepository 는 GoogleSheets 에 Log를 저장하는 책임이 있는 클래스이다.
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

    private final GoogleSheetsClient googleSheetsClient;

    @Autowired
    public LogGoogleSheetsRepository(
        GoogleSheetsClient googleSheetsClient
    ) {
        this.googleSheetsClient = googleSheetsClient;
    }

    public String saveJobDetailLog(JobDetailLog jobDetailLog) throws Exception {
        String[] jobDetailLogStrings = convertObjectToStringArray(jobDetailLog);

        AppendValuesResponse appendValuesResponse = googleSheetsClient.append(jobDetailLog.getClass().getSimpleName(), jobDetailLogStrings);

        return appendValuesResponse.getUpdates().getUpdatedRange();
    }


    public void saveStepDetailLog(StepDetailLog stepDetailLog) throws Exception {
        String[] stepDetailLogStrings = convertObjectToStringArray(stepDetailLog);

        googleSheetsClient.append(stepDetailLog.getClass().getSimpleName(), stepDetailLogStrings);
    }

    public void saveGithubApiLog(GithubApiLog githubApiLog) throws Exception {
        String[] githubApiLogStrings = convertObjectToStringArray(githubApiLog);

        googleSheetsClient.append(githubApiLog.getClass().getSimpleName(), githubApiLogStrings);
    }

    public void saveExecutionTimeLog(ExecutionTimeLog executionTimeLog) throws Exception {
        String[] executionTimeLogStrings = convertObjectToStringArray(executionTimeLog);

        googleSheetsClient.append(executionTimeLog.getClass().getSimpleName(), executionTimeLogStrings);
    }

    public void remove(String range) throws Exception {
        googleSheetsClient.remove(range);
    }

    /**
     * convertObjectToArray 는 객체의 모든 필드 값을 String 배열로 만드는 메서드이다.
     *
     * 이 메서드가 필요한 이유는 객체 데이터를 그대로 저장하기 보다, 구글 시트의 형식에 맞게 객체 데이터를 평탄화(flatten)하여 저장하기 위해서 필요한다.
     *
     *
     *
     * < 주의 사항 >
     * 1. logData 내부에 필드로 커스텀 객체가 없도록 해야 한다. 객체가 있으면, 추가로 평탄화 작업이 필요하므로, 로직이 복잡해진다.
     * 2. logData의 타입이 곧 구글 시트 탭 이름이 될 수 있도록 하고, 필드의 선언 순서도 저장되는 순서임을 기억해야 한다. 또한,, XXXLog 형태로 클래스를 만들것!
     */
    private <T> String[] convertObjectToStringArray(T obj) throws Exception {
        try {
            Class<?> clazz = obj.getClass(); // 객체의 클래스 정보를 얻음

            Field[] fields = clazz.getDeclaredFields(); // 클래스의 모든 필드 정보를 얻음

            String[] values = new String[fields.length]; // 필드 값을 저장할 배열을 생성

            for (int i = 0; i < fields.length; i++) { // 각 필드의 값을 배열에 저장
                fields[i].setAccessible(true); // private 필드 접근 허용
                values[i] = String.valueOf(fields[i].get(obj));
            }

            return values;
        } catch (IllegalAccessException illegalAccessException) { // TODO : 이것도 카테고리 포함 시켜야 겠네
            log.error("접근이 불가능한 필드에 접근 시도로 Object -> Array 변환 실패했습니다. exception={}", illegalAccessException.getMessage());

            throw illegalAccessException;
        } catch (Exception exception) {
            log.error("예상치 못한 예외로 Object -> Array 변환 실패했습니다. exception={}", exception.getMessage());

            throw exception;
        }
    }
}
