package com.codaily.common.gpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient langchainWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8000")  // FastAPI 서버 주소
                .build();
    }
}