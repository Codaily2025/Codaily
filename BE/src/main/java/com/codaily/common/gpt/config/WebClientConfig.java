package com.codaily.common.gpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient langchainWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8000")  // FastAPI 서버 주소
                .build();
    }

    @Bean
    public WebClient githubWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.github.com") // ★ 반드시 절대 URL
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}