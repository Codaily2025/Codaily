package com.codaily.auth.config.filter;

import com.codaily.auth.service.CustomUserDetailsService;
import com.codaily.auth.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Authorization 헤더에서 토큰 추출
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");

        // 2. 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 토큰에서 사용자 정보 추출
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // 4. DB에서 사용자 정보 조회
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 5. 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 6. SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
