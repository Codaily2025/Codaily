package com.codaily.common.git.service;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GithubService {
    Mono<String> createRepository(String accessToken, String repoName);

    Mono<Map<String, Object>> getRepositoryInfo(String accessToken, String owner, String repoName);

    Mono<Map<String, Object>> fetchUserInfo(String accessToken);

    void registerWebhook(String owner, String repo, String accessToken);

    Mono<List<Map<String, Object>>> getUserRepositories(String accessToken);

    Mono<Map<String, Integer>> getRepositoryLanguages(String accessToken, String owner, String repoName);

    Mono<Set<String>> getAllTechStack(String accessToken, String username);

    Mono<List<Map<String, Object>>> getUserCommits(String accessToken, String username, String since);

    Mono<Map<String, Object>> getCommitActivity(String accessToken, String username);
    // 새로 추가: 특정 날짜의 커밋 수 조회
    Mono<Integer> getCommitsByDate(String accessToken, String username, LocalDate date);
    // 새로 추가: 특정 기간의 일별 커밋 통계 조회
    Mono<Map<LocalDate, Integer>> getDailyCommitStats(String accessToken, String username, LocalDate startDate, LocalDate endDate);
}
