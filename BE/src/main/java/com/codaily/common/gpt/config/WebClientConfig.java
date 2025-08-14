package com.codaily.common.gpt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.url.ai}")
    private String aiUrl;

    @Bean
    public WebClient langchainWebClient() {
        return WebClient.builder()
                .baseUrl(aiUrl)  // FastAPI 서버 주소
                .build();
    }

    @Bean(name = "githubWebClient")
    public WebClient githubWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.github.com") // ★ 반드시 절대 URL
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}