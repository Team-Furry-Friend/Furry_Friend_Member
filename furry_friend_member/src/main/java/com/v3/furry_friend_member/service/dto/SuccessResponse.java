package com.v3.furry_friend_member.service.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SuccessResponse {

    private final String accessToken;
}
