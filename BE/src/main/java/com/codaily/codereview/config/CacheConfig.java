package com.codaily.codereview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Boolean> idempotencyCache() {
        return com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .expireAfterWrite(java.time.Duration.ofMinutes(10)) // 10분 내 중복 차단
                .maximumSize(10_000)
                .build();
    }
}

