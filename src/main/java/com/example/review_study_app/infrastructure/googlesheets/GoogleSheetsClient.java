package com.example.review_study_app.infrastructure.googlesheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * GoogleSheetsClient 는 구글 시트와 통신하는 클라이어트 클래스이다.
 *
 * 구글 시트의 메서드는 다음 링크를 참고하자!
 * 참고 : https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets.values?hl=ko
 */
@Slf4j
@Component
public class GoogleSheetsClient {

    @Value("${google.spreadsheet.id}")
    private String SPREAD_SHEET_ID;

    private final GoogleSheetsFactory googleSheetsFactory;

    @Autowired
    public GoogleSheetsClient(GoogleSheetsFactory googleSheetsFactory) {
        this.googleSheetsFactory = googleSheetsFactory;
    }
    private Sheets getSheets() {
        Sheets sheets = googleSheetsFactory.createSheets();

        log.info("-----------------------sheets={}", sheets);

        return sheets; // TODO : Sheet Bean 으로 동록해서 사용할까?
    }

    /**
     * append 는 스프레드시트의 지정된 range에 String 배열을 추가하는 메서드이다.
     *
     * < 알아두면 좋은 사항 >
     * 1. range 의 예 : sheet1!A1:C4 -> sheet1의 A1부터 C4까지
     * 2. setValueInputOption("USER_ENTERED")에 대한 참고 링크 : https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption?hl=ko
     */
    public <T> AppendValuesResponse append(String sheetsTabName, String[] strings) throws IOException {

        String range = sheetsTabName+"!A5";

        ValueRange data = new ValueRange().setValues(Arrays.asList(Arrays.asList(strings)));

        return getSheets()
            .spreadsheets().values()
            .append(SPREAD_SHEET_ID, range, data)
            .setValueInputOption("USER_ENTERED")
            .setIncludeValuesInResponse(true)
            .execute();
    }

    /**
     * remove 는 스프레드시트의 지정된 range의 데이터를 삭제하는 메서드이다.
     */
    public ClearValuesResponse remove(String range) throws IOException {

        return getSheets()
            .spreadsheets().values()
            .clear(SPREAD_SHEET_ID, range, new ClearValuesRequest()) // TODO : ClearValuesRequest 이거 무엇이고, 왜 필요한 거지?
            .execute();
    }
}
