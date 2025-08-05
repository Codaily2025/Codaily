package com.codaily.common.gpt.dispatcher;

import com.codaily.common.gpt.handler.MessageType;
import com.codaily.common.gpt.handler.SseMessageHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class SseMessageDispatcher {
    private final List<SseMessageHandler<?>> handlers;

    public <T> T dispatch(MessageType messageType, JsonNode content, Long projectId, Long specId) {
        return handlers.stream()
                .filter(h -> h.getType().equals(messageType))
                .findFirst()
                .map(h -> ((SseMessageHandler<T>) h).handle(content, projectId, specId))
                .orElseThrow(() -> new IllegalArgumentException("No handler found for type: " + messageType));
    }

}
