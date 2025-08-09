package com.codaily.retrospective.dto;

import lombok.Data;

@Data
class RetrospectiveActionItem {
    private String title;
    private String description;
    private Integer priority;   // 1(high) ~ 5(low)
    private String owner;       // 기본은 본인, 팀 프로젝트면 지정 가능
    private String due;         // "tomorrow", "2025-08-10" 등
}
