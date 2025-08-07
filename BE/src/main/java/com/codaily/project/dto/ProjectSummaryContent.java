package com.codaily.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectSummaryContent {
    private String projectTitle;
    private String projectDescription;
    private String specTitle;
    private Long projectId;
    private Long specId;
}
