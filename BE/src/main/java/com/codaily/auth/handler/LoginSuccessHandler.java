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
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String registrationId = extractRegistrationId(request);
            String userIdentifier = getUserIdentifier(oAuth2User, registrationId);

            // 1. JWT 토큰 생성
            String token = jwtTokenProvider.createToken(userIdentifier);

            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24시간
            response.addCookie(jwtCookie);

            // 2. 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
            String redirectUrl = "http://localhost:5173/oauth/callback?token=" + token;
            log.info("Redirecting to: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Error in LoginSuccessHandler", e);
            response.sendRedirect("http://localhost:5173/login?error=authentication_failed");
        }
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/kakao")) return "kakao";
        if (requestURI.contains("/google")) return "google";
        if (requestURI.contains("/naver")) return "naver";
        return "unknown";
    }

    private String getUserIdentifier(OAuth2User oAuth2User, String registrationId) {
        switch (registrationId) {
            case "google", "naver" -> {
                return oAuth2User.getAttribute("email");
            }
            case "kakao" -> {
                return "kakao:" + oAuth2User.getAttribute("id").toString();
            }
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        }
    }
}


