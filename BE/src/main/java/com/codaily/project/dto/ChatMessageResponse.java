package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatMessageResponse {
    private String type;
    private String content;
}

