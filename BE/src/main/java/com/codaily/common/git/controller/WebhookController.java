package com.codaily.common.git.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.common.git.WebhookPayload;
import com.codaily.common.git.service.WebhookService;
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

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookPayload payload,
                                              @AuthenticationPrincipal PrincipalDetails userDetails) {
        webhookService.handlePushEvent(payload, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }
}


