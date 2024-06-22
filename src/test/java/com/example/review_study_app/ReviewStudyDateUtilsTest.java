package com.example.review_study_app;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.DayOfWeek;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewStudyDateUtilsTest")
class ReviewStudyDateUtilsTest {

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
            String actualDate = ReviewStudyDateUtils.getDateOfDayOfCurrentWeekNumberInYear(year, weekNumber, dayOfWeek, dateFormat);

            // then
            String expectedDate = "2024.01.01"; // 첫 번째 월요일이 1월 1일로 시작하는 경우

            assertEquals(expectedDate, actualDate);
        }
    }
}