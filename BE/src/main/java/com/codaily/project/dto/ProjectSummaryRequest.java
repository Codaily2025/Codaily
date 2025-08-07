package com.codaily.project.dto;

import lombok.Data;

@Data
public class ProjectSummaryRequest {
    private String projectTitle;
    private String projectDescription;
    private String specTitle;
}
