package com.codaily.common.gpt.service;

import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> streamChat(String userId, String message);
}
