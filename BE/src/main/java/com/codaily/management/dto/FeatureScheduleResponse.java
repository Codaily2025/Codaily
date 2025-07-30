package com.codaily.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureScheduleResponse {
    private Long scheduleId;
    private Long featureId;
    private String featureTitle;
    private LocalDate scheduleDate;
    private Integer allocatedHours;
    private String status;
}
