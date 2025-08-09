package com.codaily.retrospective.dto;

import lombok.Data;

@Data
public class RetrospectiveProductivityMetrics {
    private double codeQuality;
    private double productivityScore;
    private int completedFeatures;
    private int totalCommits;
}

