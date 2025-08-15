package com.codaily.codereview.dto;

import com.codaily.codereview.entity.ChangeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DiffFile {

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("patch")
    private String patch;

    @JsonProperty("change_type")
    private ChangeType changeType;
}

