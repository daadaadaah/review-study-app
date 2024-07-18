package com.example.review_study_app.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubApiMapper {

    private ObjectMapper objectMapper;

    @Autowired
    public GithubApiMapper(
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    public NewLabelName extractNewLabelNameFromResponseBody(String body) throws Exception {
        String name = extractFieldFromJsonString(body, "name").asText();

        return new NewLabelName(name);
    }

    public NewIssue extractNewIssueFromResponseBody(String body) throws Exception {
        int number = extractFieldFromJsonString(body, "number").asInt();

        String title = extractFieldFromJsonString(body, "title").asText();

        return new NewIssue(number, title);
    }

    public List<IssueToClose> extractIssueToClosListFromResponseBody(String body) throws Exception {
        List<IssueToClose> numbers = new ArrayList<>();

        try {
            JsonNode jsonNodes = this.objectMapper.readTree(body);

            for (int i = 0; i < jsonNodes.size(); i++) {
                JsonNode jsonNode = jsonNodes.get(i);

                int number = jsonNode.get("number").asInt();

                String title = jsonNode.get("title").asText();

                numbers.add(new IssueToClose(number, title));
            }
            return numbers;
        } catch (Exception exception) {
            log.error(" exception={}, body={}", exception.getMessage(), body);
            throw exception;
        }
    }

    private JsonNode extractFieldFromJsonString(String jsonString, String fieldName) throws Exception {
        log.info("JSON 문자열에서 객체를 추출합니다. fieldName={}, jsonString={}", fieldName, jsonString);

        try {

            JsonNode jsonNodeString = objectMapper.readTree(jsonString);

            JsonNode jsonNode = jsonNodeString.get(fieldName); // TODO : Null 처림

            if(jsonNode == null) {
                log.info("JSON 문자열에 해당 필드가 없습니다. fieldName={}, jsonString={}", fieldName, jsonString);
                throw new RuntimeException("Not Found Field");
            }

            return jsonNode;
        } catch (Exception exception) {
            log.error("JSON 문자열에서 객체 추출을 실패했습니다. exception={}, class={}, jsonString={}", exception, fieldName, jsonString);
            throw exception;
        }
    }
}
