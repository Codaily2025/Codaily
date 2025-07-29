package com.codaily.common.git.controller;


import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserService;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import com.codaily.common.git.service.GithubService;
import com.codaily.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
public class GithubLinkController {

    private final UserService userService;
    private final GithubService githubService;
    private final ProjectService projectService;
    private final WebClient webClient = WebClient.create();

    // GitHub OAuth 상수 정의
    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    @Value("${github.redirect-uri}")
    private String redirectUri;

    @Value("${github.token-uri}")
    private String tokenUri;

    @Value("${github.user-uri}")
    private String userUri;

    @GetMapping("/callback")
    public Mono<ResponseEntity<String>> githubCallback(
            @RequestParam("code") String code,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        log.info("code: {}", code);
        return fetchAccessToken(code)
                .flatMap(accessToken ->
                        fetchGithubProfile(accessToken)
                                .doOnNext(profile ->
                                        userService.linkGithub(userDetails.getUserId(), profile, accessToken)
                                )
                                .thenReturn(ResponseEntity.ok("GitHub 연동 완료"))
                );
    }

    private Mono<String> fetchAccessToken(String code) {
        return webClient.post()
                .uri(tokenUri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", code,
                        "redirect_uri", redirectUri
                ))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .map(body -> {
                    String token = (String) body.get("access_token");
                    if (token == null) {
                        log.error("GitHub에서 access_token을 가져오지 못했습니다. 응답 body: {}", body);
                        throw new RuntimeException("GitHub access token null");
                    }
                    return token;
                });
    }

    private Mono<GithubFetchProfileResponse> fetchGithubProfile(String accessToken) {
        return webClient.get()
                .uri(userUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GithubFetchProfileResponse.class);
    }

    @PostMapping("/repo/create")
    public Mono<ResponseEntity<String>> createRepository(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestParam String repoName,
            @RequestParam Long projectId
    ) {
        String accessToken = userService.getGithubAccessToken(userDetails.getUserId());
        return githubService.createRepository(accessToken, repoName)
                .doOnNext(repoUrl ->
                        projectService.saveRepositoryForProject(projectId, repoName, repoUrl)
                )
                .thenReturn(ResponseEntity.ok("새 리포 생성 및 프로젝트 연결 완료"));
    }

    @PostMapping("/repo/link")
    public Mono<ResponseEntity<String>> linkExistingRepository(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestParam String owner,
            @RequestParam String repoName,
            @RequestParam Long projectId
    ) {
        String accessToken = userService.getGithubAccessToken(userDetails.getUserId());

        return githubService.getRepositoryInfo(accessToken, owner, repoName)
                .doOnNext(repo -> {
                    String repoUrl = repo.get("html_url").toString();
                    log.info("repo info: " + repoName + " " + repoUrl);
                    projectService.saveRepositoryForProject(projectId, repoName, repoUrl);
                })
                .thenReturn(ResponseEntity.ok("기존 리포 연결 완료"));
    }

}