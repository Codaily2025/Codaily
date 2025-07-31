package com.codaily.common.gpt.handler;

import com.fasterxml.jackson.databind.JsonNode;

public interface SseMessageHandler {
    String getType();
    void handle(JsonNode content, Long projectId, Long specId);
}
