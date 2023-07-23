package com.v3.furry_friend_member.service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MemberResponseDTO {

    private Long mid;
    private String email;
    private String name;
    private String address;
    private String phone;
    private String social;
    private boolean del;
    private String accessToken;
    private String refreshToken;
}
