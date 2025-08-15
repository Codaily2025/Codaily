package com.codaily.codereview.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeType {
    ADDED("added"),
    MODIFIED("modified"),
    DELETED("deleted");


    private final String value;
    ChangeType(String v){ this.value = v; }

    public static ChangeType fromString(String status) {
        return switch (status.toLowerCase()) {
            case "added" -> ADDED;
            case "modified" -> MODIFIED;
            case "deleted" -> DELETED;
            default -> throw new IllegalArgumentException("Unknown change type: " + status);
        };
    }

    @JsonValue
    public String getValue(){ return value; }
}
