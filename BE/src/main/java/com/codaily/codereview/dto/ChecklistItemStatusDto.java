package com.codaily.codereview.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChecklistItemStatusDto {
    private String item;
    private boolean done;
}
