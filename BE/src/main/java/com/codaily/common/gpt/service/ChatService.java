package com.codaily.common.gpt.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {
    Flux<String> streamChat(String intent, String userId, String message);
    Mono<String> classifyIntent(String message);
}
