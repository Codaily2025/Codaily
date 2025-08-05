package com.codaily.common.gpt.dispatcher;

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
    private final List<SseMessageHandler> handlers;

    public void dispatch(String type, JsonNode content, Long projectId, Long specId) {
        handlers.stream()
                .filter(h -> h.getType().equals(type))
                .findFirst()
                .ifPresent(handler -> handler.handle(content, projectId, specId));
    }

}
