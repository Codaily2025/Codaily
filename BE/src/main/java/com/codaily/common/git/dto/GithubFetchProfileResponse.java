package com.codaily.common.git.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GithubFetchProfileResponse {

    private Long id;

    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String name;

    private String email;

    private String bio;

    private String location;
}
