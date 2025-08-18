package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FeatureInferenceRequestDto {
    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("commit_id")
    private Long commitId;

    @JsonProperty("commit_hash")
    private String commitHash;

    @JsonProperty("commit_branch")
    private String commitBranch;

    @JsonProperty("diff_files")
    private List<DiffFile> diffFiles;

    @JsonProperty("available_features")
    private List<String> availableFeatures;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("commit_message")
    private String commitMessage;

    @JsonProperty("commit_info")
    private CommitInfoDto commitInfoDto;

    @JsonProperty("force_done")
    private boolean forceDone;
}

