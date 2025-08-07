package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class CodeReviewResultRequest {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("commit_id")
    private Long commitId;

    @JsonProperty("feature_name")
    private List<String> featureNames; // nullable

    @JsonProperty("feature_id")
    private Long featureId;

    @JsonProperty("checklist_evaluation")
    private Map<String, Boolean> checklistEvaluation; // nullable

    @JsonProperty("checklist_file_map")
    private Map<String, List<String>> checklistFileMap; // nullable

    @JsonProperty("extra_implemented")
    private List<String> extraImplemented; // nullable

    @JsonProperty("code_review_items")
    private List<CodeReviewItemDto> codeReviewItems; // nullable

    @JsonProperty("review_summary")
    private Map<String, String> reviewSummary; // nullable

    @JsonProperty("review_summaries")
    private List<String> reviewSummaries; // nullable

    @JsonProperty("force_done")
    private boolean forceDone;
}
