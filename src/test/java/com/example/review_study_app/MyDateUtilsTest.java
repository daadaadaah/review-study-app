package com.example.review_study_app;

import static com.example.review_study_app.common.utils.MyDateUtils.getCurrentWeekNumber;
import static com.example.review_study_app.common.utils.MyDateUtils.getCurrentYear;
import static org.junit.jupiter.api.Assertions.*;

import com.example.review_study_app.common.utils.MyDateUtils;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewStudyDateUtilsTest")
class MyDateUtilsTest {

    @Nested
    @DisplayName("getCurrentYear()")
    class Describe_GetCurrentYear {

        @Test
        @DisplayName("returns currentYear")
        void It_returns_currentYear() throws IOException {
            // given
            ZonedDateTime zonedDateTime = ZonedDateTime.of(2024, 6, 24, 0, 10, 0, 0, ZoneId.of("Asia/Seoul"));

            // when
            int currentYear = getCurrentYear(zonedDateTime);

            // then
            assertEquals(2024, currentYear);
        }
    }

    @Nested
    @DisplayName("getCurrentWeekNumber()")
    class Describe_getCurrentWeekNumber {

        @Test
        @DisplayName("returns currentWeekNumber")
        void It_returns_currentWeekNumber() throws IOException {
            // given
            ZonedDateTime zonedDateTime = ZonedDateTime.of(2024, 6, 24, 0, 10, 0, 0, ZoneId.of("Asia/Seoul"));

            LocalDate todayInSeoul = zonedDateTime.toLocalDate();
            WeekFields weekFields = WeekFields.ISO;
            int expectedWeekNumber = todayInSeoul.get(weekFields.weekOfYear());

            // when
            int weekNumber = getCurrentWeekNumber(zonedDateTime);

            // then
            assertEquals(expectedWeekNumber, weekNumber);
        }
    }

    @Nested
    @DisplayName("getDateOfDayOfCurrentWeekNumberInYear()")
    class Describe_GetDateOfDayOfCurrentWeekNumberInYear {

        @Test
        @DisplayName("returns the date for the given year, week number, and day of week")
        void It_returns_the_date_for_the_given_year_week_number_and_day_of_week() throws IOException {
            // given
            int year = 2024;
            int weekNumber = 1;
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            String dateFormat = "yyyy.MM.dd";

            // when
            String actualDate = MyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, dayOfWeek, dateFormat);

            // then
            String expectedDate = "2024.01.01"; // 첫 번째 월요일이 1월 1일로 시작하는 경우

            assertEquals(expectedDate, actualDate);
        }
    }
}