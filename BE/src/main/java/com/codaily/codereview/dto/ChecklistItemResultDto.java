package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChecklistItemResultDto {
    @JsonProperty("feature_id")
    private Long featureId;

    @JsonProperty("checklist_items")
    private List<ChecklistItemDto> checklistItems;
}


