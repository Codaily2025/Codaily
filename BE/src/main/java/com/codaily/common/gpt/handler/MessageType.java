package com.codaily.common.gpt.handler;

public enum MessageType {
    CHAT,
    SPEC,
    SPEC_REGENERATE;

    public static MessageType fromString(String raw) {
        return switch (raw.toLowerCase()) {
            case "chat" -> CHAT;
            case "spec" -> SPEC;
            case "spec:regenerate" -> SPEC_REGENERATE;
            default -> throw new IllegalArgumentException("Unknown intent: " + raw);
        };
    }
}
