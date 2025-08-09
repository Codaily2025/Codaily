// src/main/java/com/codaily/auth/controller/AuthController.java
package com.codaily.auth.controller;

import com.codaily.auth.config.PrincipalDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Map<String, Object> response = new HashMap<>();
        
        if (principalDetails != null) {
            response.put("authenticated", true);
            // response.put("userId", principalDetails.getUserId());
            response.put("email", principalDetails.getUsername());
            response.put("nickname", principalDetails.getUser().getNickname());
            response.put("provider", principalDetails.getUser().getSocialProvider());
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("인증이 필요한 API입니다!");
    }
}
