package com.codaily.common.gpt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStreamRequest {
    private String intent;
    private String userId;
    private String message;
    private Long projectId;
    private Long specId;
    private Long featureId;
    private String field;
}

