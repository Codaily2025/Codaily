package com.codaily.common.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatFilterResult {
    private final boolean allowed;
    private final String code;
    private final String message;
}
