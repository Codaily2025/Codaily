package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ManualCompleteFeatureInferenceRequestDto {
    @JsonProperty("project_id")
    private Long projectId;
    private Long featureId;
    private Long userId;
    private boolean forceDone;
}
