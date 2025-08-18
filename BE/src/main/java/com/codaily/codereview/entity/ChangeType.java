package com.codaily.codereview.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeType {
    ADDED, MODIFIED, REMOVED;

    @JsonValue
    public String toJson() {
        // 항상 "ADDED"/"MODIFIED"/"REMOVED"로 직렬화
        return name();
    }

    public static ChangeType fromGithubStatus(String status) {
        if (status == null) return MODIFIED;
        switch (status.toLowerCase()) {
            case "added": return ADDED;
            case "removed":
            case "deleted": return REMOVED;
            default: return MODIFIED; // renamed/copied 등 폴백
        }
    }
}
