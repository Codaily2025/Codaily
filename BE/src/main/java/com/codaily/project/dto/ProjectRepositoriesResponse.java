    package com.codaily.project.dto;

    import lombok.AllArgsConstructor;
    import lombok.Data;

    import java.util.List;

    @Data
    @AllArgsConstructor
    public class ProjectRepositoriesResponse {
        private List<ProjectRepositoryResponse> repositories;
    }

