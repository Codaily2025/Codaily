package com.codaily.common.gpt.handler;

import com.fasterxml.jackson.databind.JsonNode;

public interface SseMessageHandler<T> {
    MessageType getType();
    Class<T> getResponseType();
    T handle(JsonNode content, Long projectId, Long specId);
}
