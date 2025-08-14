package com.codaily.codereview.dto;

import com.codaily.codereview.entity.CodeReviewItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ManualCodeReviewRequestDto {
    @JsonProperty("project_id")
    private Long projectId;
    private String featureName;
    private List<CodeReviewItemDto> items;
}

