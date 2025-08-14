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
    private Long commitId;
    private String commitHash;
    private String commitBranch;
    private List<DiffFile> diffFiles;
    @JsonProperty("available_features")
    private List<String> availableFeatures;
    @JsonProperty("access_token")
    private String accessToken;
    private String commitMessage;

    @JsonProperty("commit_info")
    private CommitInfoDto commitInfoDto;
    private boolean forceDone;
}

