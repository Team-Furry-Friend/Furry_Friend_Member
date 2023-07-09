package com.v3.furry_friend_member.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenResponse {

    private String refreshToken;
    private String accessToken;
}
