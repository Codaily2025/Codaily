package com.codaily.common.gpt.handler;

public enum MessageType {
    CHAT,
    SPEC,
    SPEC_REGENERATE,
    PROJECT_SUMMARIZATION;

    public static MessageType fromString(String raw) {
        return switch (raw.toLowerCase()) {
            case "chat" -> CHAT;
            case "spec" -> SPEC;
            case "spec:regenerate" -> SPEC_REGENERATE;
            case "project:summarization" -> PROJECT_SUMMARIZATION;
            default -> throw new IllegalArgumentException("Unknown intent: " + raw);
        };
    }
}
