package com.codaily.codereview.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FileFetchRequestDto {
    private List<String> filePaths;
}
