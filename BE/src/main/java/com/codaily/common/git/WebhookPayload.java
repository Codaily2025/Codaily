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
    private Pusher pusher;
    private Sender sender;

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

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pusher {
        private String name;           // 일반적으로 깃허브 username (push 이벤트 전송 주체)
        private String email;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sender {
        private String login;          // 깃허브 username (가장 신뢰)
        private Long id;
        private String avatar_url;
        private String html_url;
    }


}

