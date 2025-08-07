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
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        log.info("OAuth2User attributes: " + oAuth2User.getAttributes());


        // 1. JWT 토큰 생성
        String token = jwtTokenProvider.createToken(email);

        // 2. JSON 응답으로도 넘김 (JS 방식 접근용)
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().write("{\"token\": \"" + token + "\"}");

        // 3. HttpOnly 쿠키 설정 (브라우저 자동 전송용)
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);                     // JS 접근 불가
        cookie.setPath("/");                          // 전체 경로에 대해 전송
        cookie.setMaxAge(60 * 60 * 24);               // 1일 유지 (초 단위)
        cookie.setSecure(true);                       // https 환경에서만 동작 (로컬은 false로 테스트 가능)
        response.addCookie(cookie);
    }
}


