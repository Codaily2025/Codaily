package com.codaily.common.git.controller;

import com.codaily.auth.entity.User;
import com.codaily.auth.repository.UserRepository;
import com.codaily.auth.service.UserService;
import com.codaily.common.git.WebhookPayload;
import com.codaily.common.git.service.GithubService;
import com.codaily.common.git.service.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;
    private final GithubService githubService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-GitHub-Event", required = false) String event
    ) {
        // 디버그 로그(일단 찍어보자)
        log.info("Webhook hit: event={}, len={}", event, rawBody != null ? rawBody.length() : -1);

        // 1) ping은 바로 200 (바인딩 안 함)
        if ("ping".equalsIgnoreCase(event)) {
            return ResponseEntity.ok().build();
        }

        try {
            // 2) push 등 필요한 경우에만 DTO로 파싱
            WebhookPayload payload = objectMapper.readValue(rawBody, WebhookPayload.class);

            String fullName = payload.getRepository().getFull_name(); // "owner/repo"
            String owner = fullName.split("/")[0];

            User user = userRepository.findByGithubAccount(payload.getSender().getLogin())
                            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

            webhookService.handlePushEvent(payload, user.getUserId());
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Webhook 처리 실패: event={}, body={}", event, rawBody, e);
            return ResponseEntity.internalServerError().build();
        }
    }



    @Operation(summary = "백엔드용")
    @PostMapping("/register")
    public ResponseEntity<String> testRegister() {
        String owner = "codailyTest";
        String repo = "codailyTest";
        String accessToken = "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";
        githubService.registerWebhook(owner, repo, accessToken);
        return ResponseEntity.ok("등록 완료!");
    }
}


