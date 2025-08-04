package com.codaily.common.gpt.controller;

import com.codaily.common.gpt.dispatcher.SseMessageDispatcher;
import com.codaily.common.gpt.dto.ChatMessageRequest;
import com.codaily.common.gpt.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SseMessageDispatcher dispatcher;
    private final ObjectMapper objectMapper;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("userId") String userId,
                                 @RequestParam("message") String message,
                                 @RequestParam("projectId") Long projectId,
                                 @RequestParam("specId") Long specId) {

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        Flux<String> chatFlux = chatService.streamChat(userId, message);

        chatFlux.subscribe(chunk -> {
            try {
                JsonNode root = objectMapper.readTree(chunk);
                String type = root.path("type").asText();
                JsonNode content = root.path("content");

                Object response = dispatcher.dispatch(type, content, projectId, specId);

                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(response)));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);


        return emitter;
    }
}