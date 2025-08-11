package com.codaily.mypage.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicknameUpdateRequest {
   private String nickname;
}
