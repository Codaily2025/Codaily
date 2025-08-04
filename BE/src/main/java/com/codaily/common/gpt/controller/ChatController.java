package com.codaily.common.gpt.controller;

import com.codaily.common.gpt.handler.MessageType;
import com.codaily.common.gpt.handler.ChatResponseStreamHandler;
import com.codaily.common.gpt.service.ChatService;
import com.codaily.project.service.FeatureItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Log4j2
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final FeatureItemService featureItemService;
    private final ChatResponseStreamHandler chatStreamHandler;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("userId") String userId,
                                 @RequestParam("message") String message,
                                 @RequestParam("projectId") Long projectId,
                                 @RequestParam("specId") Long specId) {
        String intent = chatService.classifyIntent(message).block();
        MessageType messageType = MessageType.fromString(intent);

        switch (messageType) {
            case SPEC:
            case SPEC_REGENERATE:
                featureItemService.deleteBySpecId(specId);
                break;
            case CHAT:
                break;
        }

        return chatStreamHandler.stream(intent, userId, message, projectId, specId);
    }
}
