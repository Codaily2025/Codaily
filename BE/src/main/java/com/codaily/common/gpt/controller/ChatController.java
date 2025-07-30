package com.codaily.common.gpt.controller;

import com.codaily.common.gpt.dto.ChatMessageRequest;
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

    private final com.codaily.common.gpt.service.ChatService chatService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("userId") String userId,
                                 @RequestParam("message") String message) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE) ;

        Flux<String> chatFlux = chatService.streamChat(userId, message);

        chatFlux.subscribe(chunk -> {
            log.info("sse chunk: "+ chunk);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> payload = Map.of("text", chunk);
                String json = objectMapper.writeValueAsString(payload);

                emitter.send(SseEmitter.event().data(json));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);

        return emitter;
    }
}
