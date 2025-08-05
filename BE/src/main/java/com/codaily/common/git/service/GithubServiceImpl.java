package com.codaily.common.git.service;

import com.codaily.codereview.dto.DiffFile;
import com.codaily.codereview.dto.FeatureInferenceRequestDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
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

    @Override
    public void registerWebhook(String owner, String repo, String accessToken) {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/hooks";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> config = Map.of(
                "url", "https://codaily.ai/api/webhook",
                "content_type", "json"
        );

        Map<String, Object> body = Map.of(
                "name", "web",
                "active", true,
                "events", List.of("push"),
                "config", config
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("✅ Webhook 등록 성공: {}", repo);
        } else {
            log.warn("❌ Webhook 등록 실패: {} - {}", repo, response.getBody());
        }
    }
}

