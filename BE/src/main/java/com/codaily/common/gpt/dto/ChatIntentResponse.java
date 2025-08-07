package com.codaily.common.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatIntentResponse {
    private String intent; // "chat", "spec", "spec:regenerate"
}

