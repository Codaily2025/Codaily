// src/main/java/com/codaily/auth/handler/LoginSuccessHandler.java
package com.codaily.auth.handler;

import com.codaily.auth.service.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        log.info("=== LoginSuccessHandler called ===");
        log.info("Request URI: " + request.getRequestURI());
        log.info("Authentication: " + authentication);
        
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");
            log.info("OAuth2User attributes: " + oAuth2User.getAttributes());

            // 1. JWT 토큰 생성
            String token = jwtTokenProvider.createToken(email);
            log.info("Generated JWT token for user: " + email);

            // 2. 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
            String redirectUrl = "http://localhost:5173/oauth/callback?token=" + token;
            log.info("Redirecting to: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } else {
            log.error("Authentication or OAuth2User is null");
            // 토큰 API 엔드포인트로 리다이렉트
            response.sendRedirect("http://localhost:8080/api/auth/oauth2/token");
        }
    }
}

