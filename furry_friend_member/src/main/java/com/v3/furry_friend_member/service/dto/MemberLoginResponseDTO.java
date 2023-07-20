package com.v3.furry_friend_member.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MemberLoginResponseDTO {

    private String accessToken;
    private String refreshToken;
}