package com.example.review_study_app;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
}
