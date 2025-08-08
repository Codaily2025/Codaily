package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class FeatureChecklistRequestDto {
    private List<FeatureChecklistFeatureDto> features; // 기능들 전체

}
