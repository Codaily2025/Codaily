package com.codaily.auth.controller;

import com.codaily.auth.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class OAuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/api/login/oauth2/code/google")
    public void handleGoogleOAuthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        
        log.info("Google OAuth callback received - code: {}, state: {}", code, state);
        
        try {
            // Spring Security의 OAuth2 인증 처리를 위해 기본 리다이렉트 엔드포인트 사용
            // 실제 인증 처리는 Spring Security가 자동으로 수행
            String redirectUrl = "http://localhost:5173/oauth/callback?code=" + code + "&state=" + state;
            log.info("Redirecting to frontend: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Error processing OAuth callback", e);
            response.sendRedirect("http://localhost:5173/login?error=server_error");
        }
    }

    @GetMapping("/api/login/oauth2/code/naver")
    public void handleNaverOAuthCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response) throws IOException {
        
        log.info("Naver OAuth callback received - code: {}, state: {}", code, state);
        
        try {
            String redirectUrl = "http://localhost:5173/oauth/callback?code=" + code + "&state=" + state;
            log.info("Redirecting to frontend: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Error processing OAuth callback", e);
            response.sendRedirect("http://localhost:5173/login?error=server_error");
        }
    }

    @PostMapping("/api/oauth/token")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getOAuthToken(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String code = request.get("code");
        String state = request.get("state");
        
        log.info("Token request received - code: {}, state: {}", code, state);
        
        try {
            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                String email = oAuth2User.getAttribute("email");
                
                // JWT 토큰 생성
                String token = jwtTokenProvider.createToken(email);
                log.info("Generated JWT token for user: {}", email);
                
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                log.error("Authentication failed or OAuth2User not found");
                return ResponseEntity.badRequest().body(Map.of("error", "authentication_failed"));
            }
        } catch (Exception e) {
            log.error("Error generating token", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "token_generation_failed"));
        }
    }
}
