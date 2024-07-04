package com.example.review_study_app.common.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MyJsonUtils {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode extractFieldFromBody(String body, String field) {

        JsonNode jsonNode = null;

        try {

            jsonNode = objectMapper.readTree(body);

        } catch (Exception exception) {
            log.error("[extractFieldFromBody] body = {}, field = {}, exception = {} ", body, field, exception);
        }

        return jsonNode.get(field);
    }

    // JSON 문자열을 제네릭 객체로 변환하는 함수
    // 참고 : https://hianna.tistory.com/631
    public static <T> T convertJsonToObject(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);

        } catch (Exception exception) {
            log.error("[convertJsonToObject] jsonString = {}, exception = {} ", jsonString, exception);

            return null;
        }
    }

    public static List<Integer> extractNumbersFromIssuesBody(String body) {
        List<Integer> numbers = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        try {

            JsonNode jsonNode = objectMapper.readTree(body);

            for (int i = 0; i < jsonNode.size(); i++) {
                numbers.add(jsonNode.get(i).get("number").asInt());
            }

        } catch (Exception exception) {
            log.error("[extractNumbersFromIssuesBody] body={}, exception = {} ", body, exception);
        }


        return numbers;
    }
}
