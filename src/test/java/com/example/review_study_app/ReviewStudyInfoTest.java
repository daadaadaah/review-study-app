package com.example.review_study_app;

import static com.example.review_study_app.common.utils.MyDateUtils.ZONE_ID_SEOUL;
import static com.example.review_study_app.common.utils.MyDateUtils.getNow;
import static org.junit.jupiter.api.Assertions.*;

import com.example.review_study_app.domain.ReviewStudyInfo;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ReviewStudyInfoTest")
class ReviewStudyInfoTest {

    @Nested
    @DisplayName("getFormattedThisWeekNumberLabelName()")
    class Describe_GetFormattedThisWeekNumberLabelName {

        @Test
        @DisplayName("returns formatted label name for the given year and week number")
        void It_returns_formatted_label_name_for_the_given_year_and_week_number() throws IOException {
            // given
            int year = 2024;
            int weekNumber = 25;

            // when

            String actualLabelName = ReviewStudyInfo.getFormattedThisWeekNumberLabelName(year, weekNumber);

            // then
            String expectedLabelName = "24년_25주차";

            assertEquals(expectedLabelName, actualLabelName);
        }
    }

    @Nested
    @DisplayName("getFormattedThisWeekNumberLabelDescription()")
    class Describe_GetFormattedThisWeekNumberLabelDescription {
        @Test
        @DisplayName("returns formatted label description for the given year and week number")
        void It_returns_formatted_label_description_for_the_given_year_and_week_number() throws IOException {
            // given
            int year = 2024;
            int weekNumber = 1;

            // when
            String actualDescription = ReviewStudyInfo.getFormattedThisWeekNumberLabelDescription(year, weekNumber);

            // then
            String expectedDescription = "2024.01.01~2024.01.07";

            assertEquals(expectedDescription, actualDescription);
        }
    }

    @Nested
    @DisplayName("getNow()")
    class Describe_GetNow {
        @Test
        @DisplayName("returns formatted date")
        void It_returns_formatted_date() throws IOException {
            // given
            ZonedDateTime now = ZonedDateTime.now(ZONE_ID_SEOUL);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String expected = now.format(formatter);

            // when
            String actual = getNow(now);

            // then
            assertEquals(expected, actual);
        }
    }
}
