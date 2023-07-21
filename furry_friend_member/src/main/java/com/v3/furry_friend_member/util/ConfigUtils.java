package com.v3.furry_friend_member.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class ConfigUtils {

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.token-uri}")
    private String kakaoAccessTokenUrl;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${naver.client-id}")
    private String naverClientId;

    @Value("${naver.redirect-uri}")
    private String naverRedirectUri;

    @Value("${naver.token-uri}")
    private String naverAccessTokenUrl;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${google.client-secret}")
    private String googleClientSercret;

    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    private final String googleAccessTokenUrl = "https://oauth2.googleapis.com/token";

    private final String googleUserInfoRequestURL = "https://oauth2.googleapis.com/token";

    //카카오 로그인 성공 후 넘어오는 데이터를 이용해서 email과 name을 추출해서 리턴하는 메서드
    public String[] getKakaoEmailAndName(Map<String, Object> paramMap){
        //카카오 계정 정보가 있는 Map을 추출
        Object value = paramMap.get("kakao_account");
        LinkedHashMap accountMap = (LinkedHashMap) value;
        String email = (String)accountMap.get("email");

        value = accountMap.get("profile");
        accountMap = (LinkedHashMap) value;
        String name = (String) accountMap.get("nickname");

        return new String[] {email, name};
    }

}