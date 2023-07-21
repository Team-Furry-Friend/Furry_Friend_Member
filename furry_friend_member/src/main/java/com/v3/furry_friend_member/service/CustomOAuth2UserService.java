package com.v3.furry_friend_member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.v3.furry_friend_member.entity.Member;
import com.v3.furry_friend_member.entity.MemberRole;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.service.dto.MemberJoinDTO;
import com.v3.furry_friend_member.service.dto.MemberLoginResponseDTO;
import com.v3.furry_friend_member.service.dto.SocialTokenResponseDTO;
import com.v3.furry_friend_member.util.ConfigUtils;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    RestTemplate restTemplate = new RestTemplate();

    private final TokenService tokenService;

    private final ConfigUtils configUtils;

    // 소셜 로그인
    public MemberLoginResponseDTO getToken(String platform, String code) {

        String accessTokenUrl = null;

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);

        switch (platform){
            case "kakao":
                accessTokenUrl = configUtils.getKakaoAccessTokenUrl();
                requestBody.add("client_id", configUtils.getKakaoClientId());
                requestBody.add("redirect_uri", configUtils.getKakaoRedirectUri());
                break;
            case "google":
                accessTokenUrl = configUtils.getGoogleAccessTokenUrl();
                requestBody.add("client_id", configUtils.getGoogleClientId());
                requestBody.add("client_secret", configUtils.getGoogleClientSercret());
                requestBody.add("redirect_uri", configUtils.getGoogleRedirectUri());
                break;
            case "naver":
                accessTokenUrl = configUtils.getNaverAccessTokenUrl();
                requestBody.add("client_id", configUtils.getNaverClientId());
                requestBody.add("redirect_uri", configUtils.getNaverRedirectUri());
                break;
        }

        SocialTokenResponseDTO socialTokenResponseDTO = restTemplate.postForObject(Objects.requireNonNull(accessTokenUrl), requestBody, SocialTokenResponseDTO.class);

        if (socialTokenResponseDTO != null){

            return MemberLoginResponseDTO.builder()
                .accessToken(socialTokenResponseDTO.getAccessToken())
                .refreshToken(socialTokenResponseDTO.getRefreshToken())
                .build();
        }else {
            log.error("CustomOAuth2UserService Failed to get access token from " + platform);
            throw new RuntimeException("Failed to get access token from " + platform);
        }
    }



    //회원가입하고 토큰을 리턴하는 메서드
    private MemberLoginResponseDTO generateDTO(String email, String name, String social, String mobile){

        //email을 가지고 데이터 찾아오기
        Optional<Member> result = memberRepository.findByEmail(email);
        if(result.isEmpty()){
            //email이 존재하지 않는 경우로 회원 가입
            Member member = Member.builder()
                .mpw(passwordEncoder.encode("1111"))
                .email(email)
                .name(name)
                .phone(mobile)
                .del(false)
                .social(social)
                .build();

            member.addRole(MemberRole.USER);
            memberRepository.save(member);

            MemberJoinDTO memberJoinDTO = MemberJoinDTO.builder()
                .mid(member.getMid())
                .name(member.getName())
                .build();
            String accessToken = tokenService.createAccessToken(memberJoinDTO);
            String refreshToken = tokenService.createRefreshToken(memberJoinDTO);

            //회원가입에 성공한 회원의 토큰 리턴
            return MemberLoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken).build();
        }else{
            //email이 존재하는 경우
            Member member = result.get();

            if (social.equals(member.getSocial())){
                MemberJoinDTO memberJoinDTO = MemberJoinDTO.builder()
                    .mid(member.getMid())
                    .name(member.getName())
                    .build();
                String accessToken = tokenService.createAccessToken(memberJoinDTO);
                String refreshToken = tokenService.createRefreshToken(memberJoinDTO);

                //회원가입에 성공한 회원의 토큰 리턴
                return MemberLoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken).build();
            }else{
                throw new RuntimeException(result.get().getSocial() + "으로 로그인 하셨습니다.");
            }
        }
    }

    //로그인 성공했을 때 호출되는 메서드
    //이메일을 가진 사용자를 찾아보고 존재하지 않다면 자동으로 회원가입 시키는 메서드 반환 generateDTO
    public MemberLoginResponseDTO memberLoginOrJoin(String platform, String accessToken) throws Exception {

        // 유저 정보 요청
        //전송할 header 작성, access_token전송
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {};

        String userInfo = null;

        switch (platform){
            case "kakao":
                userInfo = configUtils.getKakaoUserInfoUri();
                break;
            case "google":
                userInfo = configUtils.getGoogleAccessTokenUrl();
                break;
            case "naver":
                userInfo = configUtils.getNaverClientId();
                break;
        }

        // Make the HTTP request using RestTemplate
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(Objects.requireNonNull(userInfo), HttpMethod.POST, requestEntity, responseType);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info(response.getBody());
        }else{
            Thread.sleep(5000);
            response = restTemplate.exchange(userInfo, HttpMethod.POST, requestEntity, responseType);
            log.info(response.getBody());
        }

        String email = null;
        String name = null;
        String mobile = null;
        switch (platform){
            case "kakao":
                String[] li = configUtils.getKakaoEmailAndName(Objects.requireNonNull(response.getBody()));
                email = li[0];
                name = li[1];
                break;
            case "google":
                email = (String)response.getBody().get("email");
                break;
            // case "naver":
            //     paramMap = (Map<String, Object>)paramMap.get("response");
            //     email = (String) paramMap.get("email");
            //     name = (String) paramMap.get("name");
            //     mobile = (String) paramMap.get("mobile");
            //     log.info(paramMap);

        }

        return generateDTO(email, name, platform.toUpperCase(), mobile);
    }
}