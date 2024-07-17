package com.example.review_study_app.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NewGithubLabel(
    long id,
    @JsonProperty("node_id")
    String nodeId,
    String url,
    String name,
    String color,
    @JsonProperty("default")
    boolean isDefault,
    String description
) {

}
