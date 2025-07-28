package com.codaily.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final long tokenValidityInMilliseconds = 1000 * 60 * 60; // 1시간

    // jjwt 0.12.3: 키 객체로 변환 필요
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 생성
     */
    public String createToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())  // SignatureAlgorithm은 enum이 아닌 Jwts.SIG 사용
                .compact();
    }

    /**
     * JWT 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT에서 사용자 이름 추출
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}