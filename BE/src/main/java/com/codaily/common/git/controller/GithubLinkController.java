package com.codaily.common.git.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserService;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import com.codaily.common.git.service.GithubService;
import com.codaily.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.net.URI;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
@Tag(name = "Github Link API", description = "GitHub OAuth 및 리포지토리 연동 기능 제공")
public class GithubLinkController {

    private final UserService userService;
    private final GithubService githubService;
    private final ProjectService projectService;
    private final WebClient webClient = WebClient.create();

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

    @Operation(summary = "GitHub OAuth 인증 콜백", description = "GitHub로부터 발급받은 인증 코드로 accessToken을 얻고 사용자 정보를 저장")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "GitHub 계정 연동 성공"),
//            @ApiResponse(responseCode = "400", description = "잘못된 인증 코드"),
//            @ApiResponse(responseCode = "401", description = "인증 실패"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/callback")
    public Mono<ResponseEntity<Void>> githubCallback(
            @Parameter(description = "GitHub로부터 받은 인가 코드") @RequestParam("code") String code,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        log.info("code: {}", code);
        return fetchAccessToken(code)
                .flatMap(accessToken ->
                        fetchGithubProfile(accessToken)
                                .doOnNext(profile ->
                                        userService.linkGithub(userDetails.getUserId(), profile, accessToken)
                                )
                                .thenReturn(ResponseEntity.noContent().build())
                );
    }

    @Operation(summary = "새 GitHub 리포지토리 생성", description = "새 GitHub 리포지토리를 생성하고 프로젝트에 연결")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "리포지토리 생성 및 연결 성공", headers = {
                    @Header(name = "Location", description = "생성된 GitHub 리포지토리 URL")
            }),
//            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 중복된 리포 이름)"),
//            @ApiResponse(responseCode = "401", description = "인증 실패"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/repo/create")
    public Mono<ResponseEntity<Void>> createRepository(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @Parameter(description = "생성할 리포지토리 이름") @RequestParam String repoName,
            @Parameter(description = "연결할 프로젝트 ID") @RequestParam Long projectId
    ) {
        String accessToken = userService.getGithubAccessToken(userDetails.getUserId());

        return githubService.createRepository(accessToken, repoName)
                .doOnNext(repoUrl ->
                        projectService.saveRepositoryForProject(projectId, repoName, repoUrl)
                )
                .map(repoUrl -> ResponseEntity
                        .created(URI.create(repoUrl))
                        .build()
                );
    }

    @Operation(summary = "기존 GitHub 리포지토리 연결", description = "이미 존재하는 리포지토리를 프로젝트에 연결")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "기존 리포지토리 연결 성공"),
//            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
//            @ApiResponse(responseCode = "401", description = "인증 실패"),
//            @ApiResponse(responseCode = "404", description = "리포지토리를 찾을 수 없음"),
//            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/repo/link")
    public Mono<ResponseEntity<Void>> linkExistingRepository(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @Parameter(description = "리포지토리 소유자 (owner)") @RequestParam String owner,
            @Parameter(description = "리포지토리 이름") @RequestParam String repoName,
            @Parameter(description = "연결할 프로젝트 ID") @RequestParam Long projectId
    ) {
        String accessToken = userService.getGithubAccessToken(userDetails.getUserId());

        return githubService.getRepositoryInfo(accessToken, owner, repoName)
                .doOnNext(repo -> {
                    String repoUrl = repo.get("html_url").toString();
                    log.info("repo info: {} {}", repoName, repoUrl);
                    projectService.saveRepositoryForProject(projectId, repoName, repoUrl);
                })
                .thenReturn(ResponseEntity.noContent().build());
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
}
