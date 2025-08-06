package com.codaily.common.git.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@Service
public class GithubServiceImpl implements GithubService {

    private final WebClient webClient = WebClient.create();

    public Mono<String> createRepository(String accessToken, String repoName) {
        return webClient.post()
                .uri("https://api.github.com/user/repos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .bodyValue(Map.of("name", repoName, "private", false))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(repo -> (String) repo.get("html_url"));
    }

    public Mono<Map<String, Object>> getRepositoryInfo(String accessToken, String owner, String repoName) {
        log.info("Fetching repository info for: " + owner + "/" + repoName);
        return webClient.get()
                .uri("https://api.github.com/repos/" + owner + "/" + repoName)  // owner와 repoName을 사용
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Mono<Map<String, Object>> fetchUserInfo(String accessToken) {
        return webClient.get()
                .uri("https://api.github.com/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                });
    }
}

