package com.codaily.project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ProjectCreateRequest {

    // 프로젝트 기본 정보
    private String title;
    private String description;

    // 예상 기간
    private LocalDate startDate;
    private LocalDate endDate;

    // 사용자가 선택한 작업 가능 날짜들
    private List<LocalDate> availableDates;

    // 요일별 작업 가능 시간 (예: {"월": 4, "수": 6, "금": 2})
    private Map<String, Integer> workingHours;
}
