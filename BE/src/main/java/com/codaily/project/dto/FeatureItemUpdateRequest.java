package com.codaily.project.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureItemUpdateRequest {
    private String status;
    private Integer priorityLevel;
    private Double estimatedTime;
}
