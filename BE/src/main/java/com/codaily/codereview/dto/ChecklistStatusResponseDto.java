package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChecklistStatusResponseDto {
    private boolean allImplemented;
    private List<ChecklistItemStatusDto> items;
}
