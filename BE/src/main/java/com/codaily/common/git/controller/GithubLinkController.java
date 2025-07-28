package com.codaily.common.git.controller;


import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserService;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
public class GithubLinkController {

    private final UserService userService;
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
                .map(body -> (String) body.get("access_token"));
    }

    private Mono<GithubFetchProfileResponse> fetchGithubProfile(String accessToken) {
        return webClient.get()
                .uri(userUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(GithubFetchProfileResponse.class);
    }
}