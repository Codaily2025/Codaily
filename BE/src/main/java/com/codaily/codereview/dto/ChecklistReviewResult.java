package com.codaily.codereview.dto;

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
public class ChecklistReviewResult {
    private String checklistItem;
    private String summary;
    private List<Map<String, Object>> codeReviews; // category, file_path, line_range, ...
}
