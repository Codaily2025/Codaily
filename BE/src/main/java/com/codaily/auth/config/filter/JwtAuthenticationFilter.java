package com.codaily.auth.config.filter;


import com.codaily.auth.config.PrincipalDetails;
import com.codaily.auth.service.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 사용자 입력값 파싱 (username, password)
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> credentials = mapper.readValue(request.getInputStream(), Map.class);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(credentials.get("username"), credentials.get("password"));
            log.info("로그인 시도 중...");
            // 로그인 시도
            return authenticationManager.authenticate(authToken);

        } catch (IOException e) {
            throw new RuntimeException("인증 요청 파싱 실패", e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authResult
    ) throws IOException, ServletException {

        PrincipalDetails userDetails = (PrincipalDetails) authResult.getPrincipal();

        // JWT 토큰 생성
        String jwt = jwtTokenProvider.createToken(userDetails.getUsername());
//        log.info("로그인 성공!!!");
        // 응답 헤더에 토큰 전달
        response.addHeader("Authorization", "Bearer " + jwt);
        response.setContentType("application/json");
        response.getWriter().write("{\"token\": \"" + jwt + "\"}");
    }

}