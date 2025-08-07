package com.codaily.common.gpt.service;

import com.codaily.common.gpt.dto.ChatIntentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {
    Flux<String> streamChat(String intent, String userId, String message, Long featureId, String field);

    Mono<ChatIntentResponse> classifyIntent(String message, Long projectId);
}
