package com.codaily.mypage.dto;

import lombok.*;

@Data
@Builder
public class ProfileImageUploadResponse {
    private String message;
    private String imageUrl;
}
