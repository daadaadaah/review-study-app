package com.example.review_study_app.domain;

import com.example.review_study_app.common.enums.ProfileType;
import com.example.review_study_app.common.utils.MyDateUtils;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

public class ReviewStudyInfo {

    private static final String WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE = "# 이번주 목표\n"
        + "- [ ] 목표 1. \n"
        + "\n"
        + "\n"
        + "# 회고\n"
        + "## 1. 목표 1에 대한 회고\n"
        + "\n"
        + "\n"
        + "# 다음주 목표\n"
        + "1.\n";

    private static final String DAHEE_WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE = "# 이번주 목표\n"
        + "- [ ] 목표 1. \n"
        + "   - [ ] 월\n"
        + "   - [ ] 화\n"
        + "   - [ ] 수\n"
        + "   - [ ] 목\n"
        + "   - [ ] 금\n"
        + "   - [ ] 토\n"
        + "   - [ ] 일\n"
        + "# 회고\n"
        + "## 1. 목표 1에 대한 회고\n"
        + "\n"
        + "\n"
        + "# 다음주 목표\n"
        + "1.\n";

    /** 멤버 **/
    private static final List<Member> PROD_MEMBERS = Arrays.asList(
        new Member("김찬웅", "Dove-kim", WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE),
        new Member("김준우", "Junuu", WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE),
        new Member("김도엽", "BrianDYKim", WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE),
        new Member("우경준", "Jay-WKJun", WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE),
        new Member("조현준", "Tianea2160", WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE),
        new Member("곽다희", "daadaadaah", DAHEE_WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE)
    );

    private static final List<Member> LOCAL_MEMBERS = Arrays.asList(
        new Member("곽다희2", "daadaadaah", DAHEE_WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE),
        new Member("곽다희1", "daadaadaah", DAHEE_WEEKLY_REVIEW_ISSUE_BODY_TEMPLATE)
    );

    public static List<Member> getReviewStudyMembers(ProfileType profileType) {
        return profileType.equals(ProfileType.LOCAL) ? LOCAL_MEMBERS : PROD_MEMBERS;
    }

    /** 레포 **/
    private static final String PROD_REPOSITORY_NAME = "Jay-WKJun/reviewStudy";

    private static final String LOCAL_REPOSITORY_NAME = "daadaadaah/reviewStudy-scheduler";

    public static String getRepositoryName(ProfileType profileType) {
        return profileType.equals(ProfileType.LOCAL) ? LOCAL_REPOSITORY_NAME : PROD_REPOSITORY_NAME;
    }

    /** 라벨 **/
    public static final String THIS_WEEK_NUMBER_LABEL_COLOR = "000000"; // Black

    public static String getFormattedThisWeekNumberLabelName(int year, int weekNumber) {
        String nameFormat = "%s년_%d주차";

        String formattedYear = String.valueOf(year).substring(2, 4);

        return String.format(nameFormat, formattedYear, weekNumber); // 예 : 24년_25주차
    }

    public static String getFormattedThisWeekNumberLabelDescription(int year, int weekNumber) {
        String dateFormat = "yyyy.MM.dd";

        String mondayOfWeekNumber = MyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.MONDAY, dateFormat);

        String sundayOfWeekNumber = MyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.SUNDAY, dateFormat);

        return mondayOfWeekNumber+"~"+sundayOfWeekNumber; // 예 : 2024.01.01~2024.01.07
    }

    /** 이슈 **/
    public static String getFormattedWeeklyReviewIssueTitle(int year, int weekNumber, String memberFullName) {
        String dateFormat = "yyyy.MM.dd";

        String mondayOfWeekNumber = MyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.MONDAY, dateFormat);

        String sundayOfWeekNumber = MyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, DayOfWeek.SUNDAY, dateFormat);

        String currentWeekPeriod = mondayOfWeekNumber+"~"+sundayOfWeekNumber;

        return "["+currentWeekPeriod+"] "+memberFullName+" 주간 회고"; // 예 : [2024.05.27~2024.06.02] 곽다희 주간 회고
    }
}
