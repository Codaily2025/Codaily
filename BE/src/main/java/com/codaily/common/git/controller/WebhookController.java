package com.codaily.common.git.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.common.git.WebhookPayload;
import com.codaily.common.git.service.GithubService;
import com.codaily.common.git.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WebhookController {

    private final WebhookService webhookService;
    private final GithubService githubService;

    @Operation(summary = "백엔드용")
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookPayload payload,
                                              @AuthenticationPrincipal PrincipalDetails userDetails) {
        webhookService.handlePushEvent(payload, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "백엔드용")
    @PostMapping("/register")
    public ResponseEntity<String> testRegister() {
        String owner = "codailyTest";
        String repo = "codailyTest";
        String accessToken = "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"; // 👉 실제 토큰 입력
        githubService.registerWebhook(owner, repo, accessToken);
        return ResponseEntity.ok("등록 완료!");
    }
}


