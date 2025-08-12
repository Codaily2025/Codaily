package com.codaily.common.git.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserService;
import com.codaily.common.git.dto.GithubFetchProfileResponse;
import com.codaily.common.git.service.GithubService;
import com.codaily.common.git.service.WebhookService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/oauth/github")
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
    public Mono<ResponseEntity<String>> githubCallback(
            @Parameter(description = "GitHub로부터 받은 인가 코드") @RequestParam("code") String code,
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        log.info("code: {}", code);
        log.info("실제 사용되는 redirect-uri: {}", redirectUri);
        return fetchAccessToken(code)
                .flatMap(accessToken ->
                        fetchGithubProfile(accessToken)
                                .doOnNext(profile ->
                                        userService.linkGithub(userDetails.getUserId(), profile, accessToken)
                                )
                                .thenReturn(createSuccessResponse())
                )
                .onErrorReturn(createErrorResponse());
    }

    private ResponseEntity<String> createSuccessResponse() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>GitHub 연동 완료</title>
            </head>
            <body>
                <script>
                    window.opener.postMessage({
                        type: 'GITHUB_CONNECTED',
                        success: true
                    }, 'http://localhost:5173');
                    window.close();
                </script>
                <p>GitHub 연동이 완료되었습니다. 창이 자동으로 닫힙니다.</p>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    private ResponseEntity<String> createErrorResponse() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>GitHub 연동 실패</title>
            </head>
            <body>
                <script>
                    window.opener.postMessage({
                        type: 'GITHUB_ERROR',
                        success: false
                    }, 'http://localhost:5173');
                    window.close();
                </script>
                <p>GitHub 연동에 실패했습니다. 창이 자동으로 닫힙니다.</p>
            </body>
            </html>
            """;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
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
        String repoOwner = userService.getGithubUsername(userDetails.getUserId());

        return githubService.createRepository(accessToken, repoName)
                .doOnNext(repoUrl -> {
                    // 작성자: yeongenn - 리포지토리 생성 완료 후 웹훅 등록
                    githubService.registerWebhook(repoOwner, repoName, accessToken);
                    projectService.saveRepositoryForProject(projectId, repoName, repoUrl);
                })
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
//            @Parameter(description = "리포지토리 소유자 (owner)") @RequestParam String owner,
            @Parameter(description = "리포지토리 이름") @RequestParam String repoName,
            @Parameter(description = "연결할 프로젝트 ID") @RequestParam Long projectId
    ) {
        // 작성자: yeongenn
        Long userId = userDetails.getUserId();
        String owner = userService.getGithubUsername(userId);
        String accessToken = userService.getGithubAccessToken(userId);

        // String accessToken = userService.getGithubAccessToken(userDetails.getUserId());
        githubService.registerWebhook(owner, repoName, accessToken);

        return githubService.getRepositoryInfo(accessToken, owner, repoName)
                .doOnNext(repo -> {
                    String repoUrl = repo.get("html_url").toString();
                    log.info("repo info: {} {}", repoName, repoUrl);
                    projectService.saveRepositoryForProject(projectId, repoName, repoUrl);
                })
                .thenReturn(ResponseEntity.noContent().build());
    }

    @DeleteMapping ("/unlink")
    @Operation(summary = "GitHub 연동 해제", description = "GitHub 연동을 해제합니다. 기존 데이터는 유지됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "GitHub 연동 해제 성공"),
            @ApiResponse(responseCode = "400", description = "연동 해제 실패")
    })
    public ResponseEntity<Map<String, Object>> unlinkGithub(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        try {
            userService.unlinkGithub(userDetails.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "GitHub 연동이 해제되었습니다. 기존 데이터는 유지됩니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("GitHub 연동 해제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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
