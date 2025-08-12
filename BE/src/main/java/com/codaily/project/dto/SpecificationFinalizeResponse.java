package com.codaily.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificationFinalizeResponse {
    private Long projectId;
    private Long specId;
    private int deletedCount; // 삭제된 isReduced 항목 수
}
