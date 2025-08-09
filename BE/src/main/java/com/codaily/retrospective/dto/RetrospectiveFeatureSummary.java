package com.codaily.retrospective.dto;

import lombok.Data;

import java.util.List;

@Data
public class RetrospectiveFeatureSummary {
    private Long featureId;
    private String title;
    private String field;
    private int checklistCount;
    private int checklistDoneCount;
    private double codeQualityScore;
    private String summary;
    private List<RetrospectiveIssueSummary> reviewIssues;
}

