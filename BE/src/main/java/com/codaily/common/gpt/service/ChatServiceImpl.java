package com.codaily.common.gpt.service;

import com.codaily.common.gpt.dto.ChatIntentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final WebClient langchainWebClient;

    public Flux<String> streamChat(String intent, String userId, String message) {
        return langchainWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/chat/gpt/stream")
                        .queryParam("intent", intent)
                        .queryParam("user_id", userId)
                        .queryParam("message", message)
                        .build())
                .accept(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class);
    }

    public Mono<String> classifyIntent(String message) {
        return langchainWebClient.post()
                .uri("/chat/intent")
                .bodyValue(Collections.singletonMap("message", message))
                .retrieve()
                .bodyToMono(ChatIntentResponse.class)
                .map(ChatIntentResponse::getIntent);
    }

}

