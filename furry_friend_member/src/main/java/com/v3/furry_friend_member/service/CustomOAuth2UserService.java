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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.v3.furry_friend_member.entity.Member;
import com.v3.furry_friend_member.entity.MemberRole;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.service.dto.MemberJoinDTO;
import com.v3.furry_friend_member.service.dto.MemberLoginResponseDTO;
import com.v3.furry_friend_member.service.dto.SocialTokenResponseDTO;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

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

    @Value("${google.redirect-uri}")
    private String googleAccessTokenUrl;

    RestTemplate restTemplate = new RestTemplate();

    private final TokenService tokenService;

    // 소셜 로그인
    public MemberLoginResponseDTO getToken(String platform, String code) {

        String accessTokenUrl = null;

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);

        switch (platform){
            case "kakao":
                accessTokenUrl = kakaoAccessTokenUrl;
                requestBody.add("client_id", kakaoClientId);
                requestBody.add("redirect_uri", kakaoRedirectUri);
                break;
            case "google":
                accessTokenUrl = googleAccessTokenUrl;
                requestBody.add("client_id", googleClientId);
                break;
            case "naver":
                accessTokenUrl = naverAccessTokenUrl;
                requestBody.add("client_id", naverClientId);
                requestBody.add("redirect_uri", naverRedirectUri);
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

    //카카오 로그인 성공 후 넘어오는 데이터를 이용해서 email과 name을 추출해서 리턴하는 메서드
    private String[] getKakaoEmailAndName(Map<String, Object> paramMap){
        //카카오 계정 정보가 있는 Map을 추출
        Object value = paramMap.get("kakao_account");
        LinkedHashMap accountMap = (LinkedHashMap) value;
        String email = (String)accountMap.get("email");

        value = accountMap.get("profile");
        accountMap = (LinkedHashMap) value;
        String name = (String) accountMap.get("nickname");

        return new String[] {email, name};
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

        // Make the HTTP request using RestTemplate
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(kakaoUserInfoUri, HttpMethod.POST, requestEntity, responseType);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info(response.getBody());
        }else{
            Thread.sleep(5000);
            response = restTemplate.exchange(kakaoUserInfoUri, HttpMethod.POST, requestEntity, responseType);
            log.info(response.getBody());
        }

       // //계정에 대한 정보 가져오기
       //  OAuth2User oAuth2User = super.loadUser(userRequest);
       //  Map<String, Object> paramMap = oAuth2User.getAttributes();
       //
        String email = null;
        String name = null;
        String mobile = null;
        switch (platform){
            case "kakao":
                String[] li = getKakaoEmailAndName(Objects.requireNonNull(response.getBody()));
                email = li[0];
                name = li[1];
                break;
            // case "google":
            //     email = (String)paramMap.get("email");
            //     break;
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