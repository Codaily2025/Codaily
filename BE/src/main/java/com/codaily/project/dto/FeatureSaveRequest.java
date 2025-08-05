package com.codaily.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FeatureSaveRequest {

    @JsonProperty("function_group")
    private String functionGroup;

    @JsonProperty("main_function")
    private MainFunction mainFunction;

    @JsonProperty("sub_functions")
    private List<SubFunction> subFunctions;

    @Data
    public static class MainFunction {
        @JsonProperty("기능명")
        private String title;

        @JsonProperty("설명")
        private String description;
    }

    @Data
    public static class SubFunction {
        @JsonProperty("상세기능명")
        private String title;

        @JsonProperty("설명")
        private String description;

        @JsonProperty("예상시간")
        private Double estimatedTime;

        @JsonProperty("우선순위")
        private Integer priorityLevel;
    }
}

