package com.codaily.common.gpt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final WebClient langchainWebClient;

    public Flux<String> streamChat(String userId, String message) {
        return langchainWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/chat/gpt/stream")
                        .queryParam("user_id", userId)
                        .queryParam("message", message)
                        .build())
                .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }
}

