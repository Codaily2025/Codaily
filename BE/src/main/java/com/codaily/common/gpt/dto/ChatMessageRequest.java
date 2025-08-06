package com.codaily.common.gpt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private String userId;
    private String message;
}
