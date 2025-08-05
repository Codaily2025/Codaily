package com.codaily.common.git.service;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface GithubService {
    public Mono<String> createRepository(String accessToken, String repoName);

    public Mono<Map<String, Object>> getRepositoryInfo(String accessToken, String owner, String repoName);

    public Mono<Map<String, Object>> fetchUserInfo(String accessToken);
}
