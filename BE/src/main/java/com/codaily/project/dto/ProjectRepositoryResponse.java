package com.codaily.project.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ProjectRepositoryResponse {
    private Long repoId;
    private String repoName;
    private String repoUrl;
    private LocalDateTime createdAt;
}

