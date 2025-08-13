package com.codaily.common.git.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubRepositoriesResponse {
    private List<GithubRepositoryResponse> repositories;
}
