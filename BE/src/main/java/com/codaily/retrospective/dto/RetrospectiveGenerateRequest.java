package com.codaily.retrospective.dto;

import com.codaily.retrospective.service.RetrospectiveTriggerType;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class RetrospectiveGenerateRequest {
    private LocalDate date;
    private Long projectId;
    private Long userId;
    private RetrospectiveTriggerType triggerType;
    private List<RetrospectiveFeatureSummary> completedFeatures;
    private RetrospectiveProductivityMetrics productivityMetrics;
}

