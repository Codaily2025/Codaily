package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProjectSpecOverviewResponse {
    private ProjectSummaryContent project;
    private List<FeatureSaveContent> features;
}
