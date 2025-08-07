package com.codaily.common.git.service;

import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GithubService {
    public Mono<String> createRepository(String accessToken, String repoName);

    public Mono<Map<String, Object>> getRepositoryInfo(String accessToken, String owner, String repoName);

    public Mono<Map<String, Object>> fetchUserInfo(String accessToken);

    public void registerWebhook(String owner, String repo, String accessToken);

    public Mono<List<Map<String, Object>>> getUserRepositories(String accessToken);

    public Mono<Map<String, Integer>> getRepositoryLanguages(String accessToken, String owner, String repoName);

    public Mono<Set<String>> getAllTechStack(String accessToken, String username);

    public Mono<List<Map<String, Object>>> getUserCommits(String accessToken, String username, String since);

    public Mono<Map<String, Object>> getCommitActivity(String accessToken, String username);
}
