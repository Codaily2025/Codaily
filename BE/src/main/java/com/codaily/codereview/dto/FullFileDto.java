package com.codaily.codereview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FullFileDto {
    private String filePath;
    private String content;
}
