package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class FeatureChecklistExtraRequestDto {
    private String projectName;
    private List<FeatureChecklistFeatureDto> features; // 기능들 전체

}
