package com.codaily.codereview.entity;

public enum ChangeType {
    ADDED,
    MODIFIED,
    REMOVED;

    public static ChangeType fromString(String status) {
        return switch (status.toLowerCase()) {
            case "added" -> ADDED;
            case "modified" -> MODIFIED;
            case "removed" -> REMOVED;
            default -> throw new IllegalArgumentException("Unknown change type: " + status);
        };
    }
}
