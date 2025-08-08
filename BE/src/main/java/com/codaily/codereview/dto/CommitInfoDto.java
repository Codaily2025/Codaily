package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommitInfoDto {
    private String repoName;
    private String repoOwner;
}
