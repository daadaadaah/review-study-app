package com.example.review_study_app.task.mapper;

import com.example.review_study_app.step.dto.IssueToClose;
import com.example.review_study_app.step.dto.NewIssue;
import com.example.review_study_app.step.dto.NewLabelName;
import com.example.review_study_app.step.exception.MyJsonParseFailException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GithubApiResponseMapper {

    private ObjectMapper objectMapper;

    @Autowired
    public GithubApiResponseMapper(
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
            log.error("JSON 문자열에서 IssueToClosList 추출을 실패했습니다. exception={}, body={}", exception.getMessage(), body);

            throw new MyJsonParseFailException(exception);
        }
    }

    private JsonNode extractFieldFromJsonString(String jsonString, String fieldName) throws Exception {
        log.info("JSON 문자열에서 필드 추출을 시작합니다. fieldName={}, jsonString={}", fieldName, jsonString);

        try {

            JsonNode jsonNodeString = objectMapper.readTree(jsonString);

            JsonNode jsonNode = jsonNodeString.get(fieldName); // TODO : Null 처림

            if(jsonNode == null) {
                log.error("JSON 문자열에서 해당 필드가 없습니다. fieldName={}, jsonString={}", fieldName, jsonString);

                throw new FieldNotFoundException("JSON 문자열에서 해당 필드가 없습니다. fieldName : "+fieldName);
            } else {
                log.info("JSON 문자열에서 필드 추출을 성공했습니다. fieldName={}, extractedValue={}", fieldName, jsonNode.toString());
            }

            return jsonNode;
        } catch (Exception exception) {
            log.error("JSON 문자열에서 필드를 추출을 실패했습니다. fieldName={}, exception={}, jsonString={}", fieldName, exception.getMessage(), jsonString);

            throw new MyJsonParseFailException(exception);
        }
    }

    /**
     * FieldNotFoundException는 Json 문자열에서 추출해내려는 필드가 없을 때, 발생하는 예외 클래스이다.
     * 일단, extractFieldFromJsonString에서만 사용하기 떄문에, inner 클래스로 만들었다.
     * 만약, extractFieldFromJsonString를 따로 JsonUtil 클래스에 구현할 경우에는 외부 클래스로 고려해보는 것이 좋다.
     */
    class FieldNotFoundException extends RuntimeException {
        public FieldNotFoundException(String message) {
            super(message);
        }
    }
}
