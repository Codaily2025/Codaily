// src/main/java/com/codaily/auth/controller/AuthController.java
package com.codaily.auth.controller;

import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/me")
    public ResponseEntity<String> getMe(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails != null) {
            return ResponseEntity.ok("인증된 사용자: " + principalDetails.getUsername());
        }
        return ResponseEntity.ok("인증이 필요한 API입니다!");
    }

    @GetMapping("/oauth2/token")
    public ResponseEntity<Map<String, String>> getOAuth2Token(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User != null) {
            String email = oAuth2User.getAttribute("email");
            String token = jwtTokenProvider.createToken(email);
            
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put("email", email);
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "OAuth2 user not found");
            return ResponseEntity.badRequest().body(error);
        }
    }
}