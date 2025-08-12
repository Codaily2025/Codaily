package com.codaily.common.gpt.handler;

public enum MessageType {
    CHAT,
    SPEC,
    SPEC_REGENERATE,
    SPEC_ADD_FEATURE_MAIN,
    SPEC_ADD_FEATURE_SUB,
    PROJECT_SUMMARIZATION,
    SPEC_ADD_FIELD,
    DELETE;

    public static MessageType fromString(String raw) {
        return switch (raw.toLowerCase()) {
            case "chat" -> CHAT;
            case "spec" -> SPEC;
            case "spec:regenerate" -> SPEC_REGENERATE;
            case "spec:add:field" -> SPEC_ADD_FIELD;
            case "spec:add:feature:main" -> SPEC_ADD_FEATURE_MAIN;
            case "spec:add:feature:sub" -> SPEC_ADD_FEATURE_SUB;
            case "project:summarization" -> PROJECT_SUMMARIZATION;
            case "delete" -> DELETE;
            default -> throw new IllegalArgumentException("Unknown intent: " + raw);
        };
    }
}
