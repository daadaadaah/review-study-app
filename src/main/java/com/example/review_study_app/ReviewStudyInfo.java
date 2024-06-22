package com.example.review_study_app;

import java.time.DayOfWeek;

public class ReviewStudyInfo {

    /** 레포 **/
    public static final String REPOSITORY_NAME = "daadaadaah/reviewStudy-scheduler"; // TODO : 테스트용 레포

    /** 라벨 **/
    public static final String THIS_WEEK_NUMBER_LABEL_COLOR = "000000"; // Black

    public static String getFormattedThisWeekNumberLabelName(int year, int weekNumber) {
        String nameFormat = "%s년_%d주차";

        String formattedYear = String.valueOf(year).substring(2, 4);

        return String.format(nameFormat, formattedYear, weekNumber); // 예 : 24년_25주차
    }

    public static String getFormattedThisWeekNumberLabelDescription(int year, int weekNumber) {
        String dateFormat = "yyyy.MM.dd";

        String mondayOfWeekNumber = ReviewStudyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.MONDAY, dateFormat);

        String sundayOfWeekNumber = ReviewStudyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.SUNDAY, dateFormat);

        return mondayOfWeekNumber+"~"+sundayOfWeekNumber; // 예 : 2024.01.01~2024.01.07
    }
}
