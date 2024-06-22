package com.example.review_study_app;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

public class MyDateUtils {

    private static final ZonedDateTime seoulDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")); // 서버가 서울이 아니라 다른 나라에 있을 수도 있어서

    // 현재 년도를 구하는 함수
    public static int getCurrentYear() {
        int currentYear = seoulDateTime.getYear();

        return currentYear;
    }

    /**
     * 이번주가 올해의 몆주차인지 구하는 함수
     * - 해당 주차 계산은 ISO 8601 기준에 따릅니다.
     * - ISO 기준에 따라 월요일이 주의 첫날 입니다.
     * - 아래 코드로 한국 기준으로 하면, 다르게 나옴.
     * WeekFields weekFields = WeekFields.of(Locale.KOREA)
     */
    public static int getCurrentWeekNumber() {
        LocalDate todayInSeoul = seoulDateTime.toLocalDate();

        WeekFields weekFields = WeekFields.ISO; // TODO : ReviewStudyInfo로 뺄까?

        int weekNumber = todayInSeoul.get(weekFields.weekOfYear());

        return weekNumber;
    }

    // 원하는 형식대로, 특정 년도의 특정 주차의 특정 요일의 날짜를 구하는 함수
    public static String getDateOfDayOfCurrentWeekNumberInYear(int year, int weekNumber, DayOfWeek dayOfWeek, String dateFormat) {
        LocalDate date = LocalDate.of(year, 1, 1)
            .with(TemporalAdjusters.firstInMonth(dayOfWeek))
            .plusWeeks(weekNumber - 1);

        return date.format(DateTimeFormatter.ofPattern(dateFormat));
    }
}
