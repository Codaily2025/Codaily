package com.codaily.retrospective.dto;

import lombok.Data;

@Data
public class RetrospectiveIssueSummary {
    private String featureTitle;
    private String checklistItem;
    private String category;
    private String severity;
    private String message;
    private String filePath;
}

