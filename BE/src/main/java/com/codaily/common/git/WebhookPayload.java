package com.codaily.common.git;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
    private String ref;
    private String before;
    private String after;
    private Repository repository;
    private List<Commit> commits;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Repository {
        private String name;
        private String full_name;
        private String html_url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String id; // SHA
        private String message;
        private String timestamp;
        private String url;
        private Author author;
        private List<String> added;
        private List<String> removed;
        private List<String> modified;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String name;
        private String email;
    }
}

