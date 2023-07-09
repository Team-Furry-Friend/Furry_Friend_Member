package com.v3.furry_friend_member.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberLoginRequestDTO {

    private String username;
    private String password;
}
