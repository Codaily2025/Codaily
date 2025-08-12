package com.codaily.mypage.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ScheduleResponse {
    private Long scheduleId;
    private LocalDate scheduledDate;
}


