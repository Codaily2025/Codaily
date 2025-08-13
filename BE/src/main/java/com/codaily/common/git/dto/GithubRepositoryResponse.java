package com.codaily.common.git.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoryResponse {
    private String name;
    private String htmlUrl;
    private String description;
    private boolean isPrivate;
}
