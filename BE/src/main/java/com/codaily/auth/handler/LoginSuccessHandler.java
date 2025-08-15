// src/main/java/com/codaily/auth/handler/LoginSuccessHandler.java
package com.codaily.auth.handler;

import com.codaily.auth.service.JwtTokenProvider;
import com.codaily.auth.repository.UserRepository;
import com.codaily.auth.entity.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Log4j2
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    // 작성자: yeongenn - UserRepository 추가
    private final UserRepository userRepository;
    @Value("${app.frontend-url}")
    private String frontendUrl;

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


            // // 2. 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
            // String redirectUrl = "http://localhost:5173/oauth/callback?token=" + token;

            // 작성자: yeongenn - 최초 로그인 판별 로직 추가
            // 2. 사용자 정보 조회하여 최초 로그인인지 확인
            User user = userRepository.findByEmail(userIdentifier).orElse(null);
            String redirectUrl;

            if (user != null && user.isFirstLogin()) {
                // 최초 로그인인 경우 추가 정보 입력 페이지로 리다이렉트
              //  redirectUrl = "http://localhost:5173/oauth/callback?token=" + token + "&isFirstLogin=true";
                redirectUrl = buildRedirectUrl(frontendUrl, token, true);
                log.info("해당 유저는 최초 로그인임~: " + userIdentifier + ", redirecting to additional info page");
            } else {
                // 기존 사용자는 홈페이지로 리다이렉트
               // redirectUrl = "http://localhost:5173/oauth/callback?token=" + token + "&isFirstLogin=false";
                redirectUrl = buildRedirectUrl(frontendUrl, token, false);
                log.info("해당 유저는 이미 로그인한 적 있음~: " + userIdentifier + ", redirecting to home");
            }

            log.info("Redirecting to: " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("Error in LoginSuccessHandler", e);
            response.sendRedirect("http://localhost:5173/login?error=authentication_failed");
        }
    }
    private String buildRedirectUrl(String base, String token, boolean isFirstLogin) {
        return UriComponentsBuilder.fromHttpUrl(base)
                .pathSegment("oauth", "callback")
                .queryParam("token", token)
                .queryParam("isFirstLogin", isFirstLogin)
                .build(true)            // 인코딩 포함
                .toUriString();
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



