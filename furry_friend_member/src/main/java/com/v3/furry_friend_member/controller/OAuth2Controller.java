package com.v3.furry_friend_member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v3.furry_friend_member.common.ApiResponse;
import com.v3.furry_friend_member.service.CustomOAuth2UserService;
import com.v3.furry_friend_member.service.dto.MemberLoginResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
public class OAuth2Controller {

    private final CustomOAuth2UserService customOAuth2UserService;

    @GetMapping("/oauth2/{platform}")
    public ApiResponse<MemberLoginResponseDTO> getKakaoAuthorizationCode(@PathVariable("platform") String platform, @RequestParam String code) {
        try {
            MemberLoginResponseDTO memberLoginResponseDTO = customOAuth2UserService.getToken(platform, code);

            return ApiResponse.success("success", customOAuth2UserService.memberLoginOrJoin(platform, memberLoginResponseDTO.getAccessToken()));
        } catch (Exception e) {
            log.error("OAuth2Controller 소셜 로그인 실패: " + e.getMessage(), e);
            return ApiResponse.fail(400, "OAuth2Controller 소셜 로그인 실패: " + e.getMessage());
        }
    }
}