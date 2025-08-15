package com.codaily.codereview.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ChangeType {
    ADDED, MODIFIED, REMOVED;


    public static ChangeType fromGithubStatus(String status) {
        if (status == null) return MODIFIED;
        switch (status.toLowerCase()) {
            case "added": return ADDED;
            case "removed":
            case "deleted": return REMOVED;
            default: return MODIFIED; // renamed/copied 등은 MODIFIED로 폴백
        }
    }
}
