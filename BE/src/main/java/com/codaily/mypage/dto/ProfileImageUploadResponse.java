package com.codaily.mypage.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileImageUploadResponse {
    private String message;
    private String imageUrl;
}
