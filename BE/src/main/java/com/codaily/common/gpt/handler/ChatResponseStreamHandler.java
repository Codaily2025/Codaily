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
import reactor.core.Disposable;
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

        Disposable subscription = chatFlux.subscribe(chunk -> {
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
                        try {
                            emitter.send(SseEmitter.event().name("error").data("internal-error"));
                        } catch (Exception ignore) {
                        }
                        emitter.completeWithError(e);
                    }
                },
                e -> {
                    // onError
                    try {
                        emitter.send(SseEmitter.event().name("error").data("upstream-error"));
                    } catch (Exception ignore) {
                    }
                    emitter.completeWithError(e);
                },
                () -> {
                    // onComplete: 종료 알림 후 닫기
                    try {
                        emitter.send(SseEmitter.event().name("end").data("done"));
                    } catch (Exception ex) {
                        log.warn("failed to send end event", ex);
                    } finally {
                        emitter.complete();
                    }
                });

        // 자원 정리
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            try {
                emitter.send(SseEmitter.event().name("end").data("timeout"));
            } catch (Exception ignore) {
            }
            emitter.complete();
        });

        return emitter;
    }
}
