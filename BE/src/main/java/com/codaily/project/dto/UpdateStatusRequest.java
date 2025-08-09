package com.codaily.project.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    private String newStatus;
}
