package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileFetchRequestDto {
    @JsonProperty("file_paths")
    @JsonAlias({"filePaths"})
    private List<String> filePaths;

    @JsonProperty("repo_name")
    @JsonAlias({"repoOwner"})
    private String repoName;

    @JsonProperty("repo_owner")
    @JsonAlias("repo_owner")
    private String repoOwner;

    @JsonProperty("commit_branch")
    @JsonAlias("commit_branch")
    private String commitBranch;
}
