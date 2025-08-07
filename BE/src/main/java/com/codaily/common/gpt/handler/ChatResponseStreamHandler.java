package com.codaily.common.gpt.handler;

import com.codaily.common.gpt.dispatcher.SseMessageDispatcher;
import com.codaily.common.gpt.dto.ChatStreamRequest;
import com.codaily.common.gpt.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@Log4j2
@Component
@RequiredArgsConstructor
public class ChatResponseStreamHandler {

    private final ChatService chatService;
    private final SseMessageDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public SseEmitter stream(ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Flux<String> chatFlux = chatService.streamChat(
                request.getIntent(),
                request.getUserId(),
                request.getMessage(),
                request.getFeatureId(),
                request.getField()
        );

        chatFlux.subscribe(chunk -> {
            try {
                JsonNode root = objectMapper.readTree(chunk);
                MessageType type = MessageType.fromString(root.path("type").asText());
                JsonNode content = root.path("content");
                log.info("sse chunk: {}", content.asText());

                Object response = dispatcher.dispatch(
                        type,
                        content,
                        request.getProjectId(),
                        request.getSpecId(),
                        request.getFeatureId()
                );

                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(response)));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);

        return emitter;
    }
}
