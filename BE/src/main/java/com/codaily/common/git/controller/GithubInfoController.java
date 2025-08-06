package com.codaily.common.git.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.UserService;
import com.codaily.common.git.service.GithubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Log4j2
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
@Tag(name = "GitHub Info API", description = "GitHub 기술스택 및 스트릭 조회")
public class GithubInfoController {

    private final UserService userService;
    private final GithubService githubService;

    @GetMapping("/tech-stack")
    @Operation(summary = "DB에 저장되어 있는 기술스택 조회")
    public ResponseEntity<Map<String, Object>> getTechStack(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        try {
            Set<String> technologies = userService.getUserTechStack(userDetails.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("technologies", technologies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("기술스택 조회 실패"));
        }
    }

    @PostMapping("/tech-stack/sync")
    @Operation(summary = "깃 허브 기술스택 동기화", description = "깃 허브 레포지토리에서 사용한 모든 기술스택을 가져옵니다.")
    public ResponseEntity<Map<String, Object>> syncTechStackFromGithub(
            @AuthenticationPrincipal PrincipalDetails userDetails
    ) {
        try {
            String accessToken = userService.getGithubAccessToken(userDetails.getUserId());
            String githubUsername = userService.getGithubUsername(userDetails.getUserId());

            if (accessToken == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("GitHub 연동이 필요합니다"));
            }

            Set<String> techStack = githubService.getAllTechStack(accessToken, githubUsername).block();

            if (techStack != null) {
                userService.syncGithubTechStack(userDetails.getUserId(), techStack);

                Set<String> allTechnologies = userService.getUserTechStack(userDetails.getUserId());

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("technologies", allTechnologies);
                response.put("message", "GitHub 기술스택이 동기화되었습니다");

                return ResponseEntity.ok(response);
            }

            return ResponseEntity.badRequest().body(createErrorResponse("GitHub 데이터를 가져올 수 없습니다"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("동기화 실패: " + e.getMessage()));
        }
    }

    @PutMapping("/tech-stack")
    @Operation(summary = "기술 스택 수정", description = "사용자가 직접 기술스택을 수정할 수 있습니다.")
    public ResponseEntity<Map<String, Object>> updateTechStack(
            @AuthenticationPrincipal PrincipalDetails userDetails,
            @RequestBody Set<String> technologies
    ) {
        try {
            userService.updateCustomTechStack(userDetails.getUserId(), technologies);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("technologies", technologies);
            response.put("message", "기술스택이 성공적으로 수정되었습니다");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse("기술스택 수정 실패"));
        }
    }

    @GetMapping("/streak")
    public Mono<ResponseEntity<Map<String, Object>>> getGitHubStreak(@AuthenticationPrincipal PrincipalDetails userDetails) {
        String accessToken = userService.getGithubAccessToken(userDetails.getUserId());

        if (accessToken == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "GitHub 연동이 필요합니다")));
        }

        String githubUsername = userService.getGithubUsername(userDetails.getUserId());

        return githubService.getCommitActivity(accessToken, githubUsername)
                .map(activity -> ResponseEntity.ok(
                        Map.<String, Object>of("streak", activity)))
                .onErrorReturn(ResponseEntity.badRequest()
                        .body(Map.<String, Object>of("error", "스트릭 조회 실패")));
    }


    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }
}