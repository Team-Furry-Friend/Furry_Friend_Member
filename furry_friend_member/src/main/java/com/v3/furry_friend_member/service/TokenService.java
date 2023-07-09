package com.v3.furry_friend_member.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;

import com.v3.furry_friend_member.entity.Token;
import com.v3.furry_friend_member.handler.JwtHandler;
import com.v3.furry_friend_member.repository.TokenRepository;
import com.v3.furry_friend_member.service.dto.MemberJoinDTO;

@Service
@Log4j2
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    private final JwtHandler jwtHandler;

    @Value("${jwt.max-age.access}") // 1
    private long accessTokenMaxAgeSeconds;

    @Value("${jwt.max-age.refresh}") // 2
    private long refreshTokenMaxAgeSeconds;

    @Value("${jwt.key.access}") // 3
    private String accessKey;

    @Value("${jwt.key.refresh}") // 4
    private String refreshKey;

    public String createAccessToken(MemberJoinDTO memberJoinDTO) {
        return jwtHandler.createToken(accessKey, memberJoinDTO, accessTokenMaxAgeSeconds);
    }

    public String createRefreshToken(MemberJoinDTO memberJoinDTO) {
        return jwtHandler.createToken(refreshKey, memberJoinDTO, refreshTokenMaxAgeSeconds);
    }

    // 토큰 검증 및 데이터 반환
    public MemberJoinDTO validateToken(String token) {
        Optional<Claims> result = jwtHandler.parse(accessKey, token);

        Long memberId = null;
        String name = null;

        if (result.isPresent()){
            Claims claims = result.get();
            memberId = claims.get("memberId", Long.class);
            name = claims.get("name", String.class);
        }

        return MemberJoinDTO.builder()
            .mid(memberId)
            .name(name).build();
    }
    
    // 로그인시 첫 토큰 저장
    public void saveToken(String accessToken, String refreshToken){

        Long memberId = validateToken(accessToken).getMid();
        Optional<Token> tokenResult = tokenRepository.findTokenByUserId(memberId);
        Token token;

        // 이전 기록이 있다면 업데이트, 없다면 생성
        if (tokenResult.isPresent()) {

            token = tokenResult.get();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            log.info("(재) 유저 아이디: {}, 발급한 AccessToken: {}, RefreshToken: {}, 발급 시간: {}", memberId, accessToken, refreshToken, LocalDateTime.now());
        } else {

            token = Token.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(memberId)
                .build();
            log.info("(새) 유저 아이디: {}, 발급한 AccessToken: {}, RefreshToken: {}, 발급 시간: {}", memberId, accessToken, refreshToken, LocalDateTime.now());
        }

        tokenRepository.save(token);
    }
}