package com.codaily.mypage.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProjectStatusResponse {
    private Long projectId;
    private String status;
}
