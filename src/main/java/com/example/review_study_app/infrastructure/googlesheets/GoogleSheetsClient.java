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
 * < 알아두면 좋은 사항 >
 * 1. 구글 시트의 메서드는 다음 링크를 참고하자!
 * 참고 : https://developers.google.com/sheets/api/reference/rest/v4/spreadsheets.values?hl=ko
 *
 * 2. 구글 시트 API 한도
 * - 참고 링크 : https://cloud.google.com/docs/quotas/view-manage?hl=ko#managing_your_quota_console
 * - 1개의 유저가 1분당 최대 쓰기 요청의 수 : 60
 *
 */
@Slf4j
@Component
public class GoogleSheetsClient {

    @Value("${google.spreadsheet.id}")
    private String SPREAD_SHEET_ID;

    private final Sheets sheets;

    /**
     * < Bean으로 등록하지 않고, 다음 코드처럼 생성자에서 Google Sheets 객체를 만들어주는 방법도 있을 수 있는데, Bean으로 등록해서 주입 받는 이유는? >
     *     @Autowired
     *     public GoogleSheetsClient(GoogleSheetsFactory googleSheetsFactory) {
     *         this.sheets = googleSheetsFactory.createSheets();
     *     }
     *
     * 1. 의존성 때문에
     * - 생성자에서 `GoogleSheetsFactory`를 사용하여 `Sheets` 객체를 직접 생성하면, `GoogleSheetsClient` 클래스는 `GoogleSheetsFactory`와 `Sheets` 2가지 의존성을 가진다.
     * - 이로 인해 객체의 생성과 생명주기를 개발자가 명시적으로 관리해야 하는 번거로움이 있다.
     *   (-> 프로젝트 상황상 객체의 생성과 생명주기를 개발자가 직접 관리해줘야하는 경우에는 위 코드가 더 적합할 수 있다)
     * - 반면, Bean으로 등록하면, Spring의 의존성 주입 메커니즘을 통해 `Sheets` 객체를 관리할 수 있다.
     * - 이로 인해 `GoogleSheetsClient` 클래스는 `Sheets` 객체의 생명주기와 관리에 대해 Spring이 대신 관리해 주므로, 개발자가 그부분을 신경쓰지 않아도 되는 편리함이 있다.
     *
     * 2. 재사용성 때문에
     * - 생성자에서 `GoogleSheetsFactory`를 사용하여 `Sheets` 객체를 직접 생성하면, 다른 곳에서 Sheets를 재사용할 수 없다.
     *   (-> 프로젝트 상황상 다양한 방식의 `Sheets` 객체가 필요하면, 위 코드가 더 적합할 수 있다)
     * - 반면, Bean으로 등록하면,  같은 `Sheets` 객체를 여러 곳에서 재사용할 수 있다.
     *
     */
    @Autowired
    public GoogleSheetsClient(Sheets sheets) {
        this.sheets = sheets;
    }

    private Sheets getSheets() {
        return sheets;
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
