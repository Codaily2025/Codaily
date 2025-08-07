package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewItemDto {

    @JsonProperty("file_path")
    private String filePath;

    @JsonProperty("line_range")
    private String lineRange;

    private String severity;

    private String message;
}
