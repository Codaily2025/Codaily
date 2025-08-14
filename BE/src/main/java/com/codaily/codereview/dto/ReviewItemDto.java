package com.codaily.codereview.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReviewItemDto {

    @JsonProperty("file_path")
    @JsonAlias("filePath")
    private String filePath;

    @JsonProperty("line_range")
    @JsonAlias("lineRange")
    private String lineRange;

    private String severity;

    private String message;
}
