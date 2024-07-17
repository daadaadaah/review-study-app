package com.example.review_study_app.github;

import java.util.List;

/**
 * NewGithubIssue 클래스는 Github 에서 새로운 Issue가 생성되고 받은 응답을 담는 클래스이다.
 */
public record NewGithubIssue(
    String url,
    String repository_url,
    String labels_url,
    String comments_url,
    String events_url,
    String html_url,
    long id,
    String node_id,
    int number,
    String title,
    User user,
    List<Label> labels,
    String state,
    boolean locked,
    User assignee,
    List<User> assignees,
    Milestone milestone,
    int comments,
    String created_at,
    String updated_at,
    String closed_at,
    String author_association,
    String active_lock_reason,
    String body,
    User closed_by,
    Reactions reactions,
    String timeline_url,
    String performed_via_github_app,
    String state_reason
) {
    public record User(
        String login,
        long id,
        String node_id,
        String avatar_url,
        String gravatar_id,
        String url,
        String html_url,
        String followers_url,
        String following_url,
        String gists_url,
        String starred_url,
        String subscriptions_url,
        String organizations_url,
        String repos_url,
        String events_url,
        String received_events_url,
        String type,
        boolean site_admin
    ) {}

    public record Label(
        long id,
        String node_id,
        String url,
        String name,
        String color,
        boolean defaultLabel,
        String description
    ) {}

    public record Milestone(
        String url,
        String html_url,
        String labels_url,
        long id,
        String node_id,
        int number,
        String state,
        String title,
        String description,
        User creator,
        int open_issues,
        int closed_issues,
        String created_at,
        String updated_at,
        String closed_at,
        String due_on
    ) {}

    public record Reactions(
        String url,
        int total_count,
        int plus_one,
        int minus_one,
        int laugh,
        int hooray,
        int confused,
        int heart,
        int rocket,
        int eyes
    ) {}
}
