package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeReviewItemDto {
    private String category;

    @JsonProperty("checklist_item")
    private String checklistItem;

    private List<ReviewItemDto> items;
}
