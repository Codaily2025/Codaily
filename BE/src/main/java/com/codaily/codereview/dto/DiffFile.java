package com.codaily.codereview.dto;

import com.codaily.codereview.entity.ChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiffFile {
    private String filePath;
    private String patch;
    private ChangeType changeType;
}

