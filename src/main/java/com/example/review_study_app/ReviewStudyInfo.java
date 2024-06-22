package com.example.review_study_app;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

public class ReviewStudyInfo {

    /** 멤버 **/
    public static final List<Member> MEMBERS = Arrays.asList(
        new Member("노경태", "ilgolf"), // TODO : 테스트시, 실패용 데이터
        new Member("곽다희", "daadaadaah")
    );

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

    /** 이슈 **/
    public static String getFormattedWeeklyReviewIssueTitle(int year, int weekNumber, String memberFullName) {
        String dateFormat = "yyyy.MM.dd";

        String mondayOfWeekNumber = ReviewStudyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.MONDAY, dateFormat);

        String sundayOfWeekNumber = ReviewStudyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.SUNDAY, dateFormat);

        String currentWeekPeriod = mondayOfWeekNumber+"~"+sundayOfWeekNumber;

        return "["+currentWeekPeriod+"] "+memberFullName+" 주간 회고"; // 예 : [2024.05.27~2024.06.02] 곽다희 주간 회고
    }

    public static final String WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE = "# 이번주 목표\n"
        + "- [ ] 목표 1. \n"
        + "\n"
        + "\n"
        + "# 회고\n"
        + "## 1. 목표 1에 대한 회고\n"
        + "\n"
        + "\n"
        + "# 다음주 목표\n"
        + "1.\n";
}
