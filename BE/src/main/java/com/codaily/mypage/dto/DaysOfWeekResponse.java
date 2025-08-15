package com.codaily.mypage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DaysOfWeekResponse {
    private Long dayId;
    private String dateName;
    private Double hours;
}
