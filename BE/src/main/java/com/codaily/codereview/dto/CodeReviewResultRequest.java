package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CodeReviewResultRequest {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("commit_id")
    private Long commitId;

    @JsonProperty("commit_hash")
    private String commitHash;

    @JsonProperty("feature_name")
    private String featureName; // nullable

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

    @JsonProperty("review_summaries")
    private Map<String, String> reviewSummaries; // nullable

    @JsonProperty("review_summary")
    private String reviewSummary; // nullable

    @JsonProperty("force_done")
    private boolean forceDone;
}
