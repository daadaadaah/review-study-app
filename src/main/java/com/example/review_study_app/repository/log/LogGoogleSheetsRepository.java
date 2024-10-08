package com.example.review_study_app.repository.log;

import com.example.review_study_app.repository.log.aop.GoogleSheetsTransactional;
import com.example.review_study_app.repository.log.entity.ExecutionTimeLog;
import com.example.review_study_app.repository.log.entity.GithubApiLog;
import com.example.review_study_app.repository.log.entity.JobDetailLog;
import com.example.review_study_app.repository.log.entity.StepDetailLog;
import com.example.review_study_app.infrastructure.googlesheets.GoogleSheetsClient;
import com.example.review_study_app.repository.log.exception.GoogleSheetsTransactionException;
import com.example.review_study_app.repository.log.exception.SaveDetailLogException;
import com.example.review_study_app.repository.log.exception.SaveExecutionTimeLogException;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import java.io.IOException;
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
public class LogGoogleSheetsRepository implements LogRepository {

    private final GoogleSheetsClient googleSheetsClient;

    @Autowired
    public LogGoogleSheetsRepository(
        GoogleSheetsClient googleSheetsClient
    ) {
        this.googleSheetsClient = googleSheetsClient;
    }

    @GoogleSheetsTransactional
    public void saveJobLogsWithTx(JobDetailLog jobDetailLog, ExecutionTimeLog executionTimeLog) {
        String newJobDetailLogRange = null;

        try {
            newJobDetailLogRange = saveJobDetailLog(jobDetailLog);

            saveExecutionTimeLog(executionTimeLog);
        } catch (SaveExecutionTimeLogException exception) {
            throw new GoogleSheetsTransactionException(exception, newJobDetailLogRange);
        }
    }

    private String saveJobDetailLog(JobDetailLog jobDetailLog) {
        try {
//            throw new RuntimeException("가짜 예외");
            String[] jobDetailLogStrings = convertObjectToStringArray(jobDetailLog);

            AppendValuesResponse appendValuesResponse = googleSheetsClient.append(jobDetailLog.getClass().getSimpleName(), jobDetailLogStrings);

            return appendValuesResponse.getUpdates().getUpdatedRange();
        } catch (Exception exception) {
            throw new SaveDetailLogException(exception);
        }
    }

    @GoogleSheetsTransactional
    public void saveStepLogsWithTx(StepDetailLog stepDetailLog, ExecutionTimeLog executionTimeLog) {
        String newStepDetailLogRange = null;

        try {
            newStepDetailLogRange = saveStepDetailLog(stepDetailLog);

            saveExecutionTimeLog(executionTimeLog);
        } catch (SaveExecutionTimeLogException exception) {
            throw new GoogleSheetsTransactionException(exception, newStepDetailLogRange);
        }
    }

    private String saveStepDetailLog(StepDetailLog stepDetailLog) {
        try {
//            throw new RuntimeException("STep 가짜 예외");
            String[] stepDetailLogStrings = convertObjectToStringArray(stepDetailLog);

            AppendValuesResponse appendValuesResponse = googleSheetsClient.append(stepDetailLog.getClass().getSimpleName(), stepDetailLogStrings);

            return appendValuesResponse.getUpdates().getUpdatedRange();
        } catch (Exception exception) {
            throw new SaveDetailLogException(exception);
        }
    }


    @GoogleSheetsTransactional
    public void saveGithubApiLogsWithTx(GithubApiLog githubApiLog, ExecutionTimeLog executionTimeLog) {
        String newGithubApiLogRange = null;

        try {
            newGithubApiLogRange = saveGithubApiLog(githubApiLog);

//            throw new SaveExecutionTimeLogException(new RuntimeException("가짜"));
            saveExecutionTimeLog(executionTimeLog);
        } catch (SaveExecutionTimeLogException exception) {
            throw new GoogleSheetsTransactionException(exception, newGithubApiLogRange);
        }
    }

    private String saveGithubApiLog(GithubApiLog githubApiLog) {
        try {
//            throw new RuntimeException("가짜 예외");
            String[] githubApiLogStrings = convertObjectToStringArray(githubApiLog);

            AppendValuesResponse appendValuesResponse = googleSheetsClient.append(githubApiLog.getClass().getSimpleName(), githubApiLogStrings);

            return appendValuesResponse.getUpdates().getUpdatedRange();
        } catch (Exception exception) {
            throw new SaveDetailLogException(exception);
        }
    }

    private void saveExecutionTimeLog(ExecutionTimeLog executionTimeLog) { // TODO : task 까지 다 하고 private로 바꿔야 할 것 같음
        try {
            String[] executionTimeLogStrings = convertObjectToStringArray(executionTimeLog);

            googleSheetsClient.append(executionTimeLog.getClass().getSimpleName(), executionTimeLogStrings);
        } catch (Exception exception) {
            throw new SaveExecutionTimeLogException(exception);
        }
    }

    public void remove(String range) throws IOException {
//        throw new IOException("가짜 IO Ex");
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
