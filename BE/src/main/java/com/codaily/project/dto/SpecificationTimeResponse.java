package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpecificationTimeResponse {
    private Long specId;
    private int totalEstimatedTime;
}

