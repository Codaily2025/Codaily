package com.codaily.common.gpt.handler;

import com.codaily.common.gpt.dispatcher.SseMessageDispatcher;
import com.codaily.common.gpt.dto.ChatFilterResult;
import com.codaily.common.gpt.dto.ChatStreamRequest;
import com.codaily.common.gpt.filter.ChatFilter;
import com.codaily.common.gpt.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
public class ChatResponseStreamHandler {

    private final ChatFilter chatFilter;
    private final ChatService chatService;
    private final SseMessageDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    public SseEmitter stream(ChatStreamRequest request) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        if(MessageType.fromString(request.getIntent()) == MessageType.CHAT_SMALLTALK) {
            request.setIntent(MessageType.CHAT.name().toLowerCase());
        }
        Flux<String> chatFlux = chatService.streamChat(
                request.getIntent(),
                request.getUserId(),
                request.getMessage(),
                request.getFeatureId(),
                request.getField()
        );

        ChatFilterResult check = chatFilter.check(MessageType.fromString(request.getIntent()),
                request.getSpecId(),
                request.getFeatureId(),
                request.getField());

        if (!check.isAllowed()) {
            try {
                var payload = Map.of("type", "chat", "content", check.getMessage());
                emitter.send(SseEmitter.event().data(payload));
                emitter.send(SseEmitter.event().name("end").data("blocked"));
            } catch (Exception ignore) {
            } finally {
                emitter.complete();
            }
            return emitter;
        }


        Disposable subscription = chatFlux.subscribe(chunk -> {
                    try {
                        JsonNode root = objectMapper.readTree(chunk);
                        MessageType type = MessageType.fromString(root.path("type").asText());
                        if(type == MessageType.CHAT_SMALLTALK) type = MessageType.CHAT;
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
