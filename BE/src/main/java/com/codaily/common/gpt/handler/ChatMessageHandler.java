package com.codaily.common.gpt.handler;

import com.codaily.project.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ChatMessageHandler implements SseMessageHandler<ChatMessageResponse> {

    @Override
    public MessageType getType() {
        return MessageType.CHAT;
    }

    @Override
    public Class<ChatMessageResponse> getResponseType() {
        return ChatMessageResponse.class;
    }

    @Override
    public ChatMessageResponse handle(JsonNode content, Long projectId, Long specId) {
        String msg = content.asText();
        return ChatMessageResponse.builder()
                .type("chat")
                .content(msg)
                .build();
    }
}

