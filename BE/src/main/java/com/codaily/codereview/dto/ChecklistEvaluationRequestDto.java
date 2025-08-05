package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChecklistEvaluationRequestDto {
    private final Long projectId;
    private final Long featureId;
    private final String featureName;
    private final List<FullFile> fullFiles;
    private final List<ChecklistItemDto> checklist;
}
