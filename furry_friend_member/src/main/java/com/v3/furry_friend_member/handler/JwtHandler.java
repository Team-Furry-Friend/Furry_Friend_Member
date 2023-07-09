package com.v3.furry_friend_member.handler;

import io.jsonwebtoken.*;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

import com.v3.furry_friend_member.service.dto.MemberJoinDTO;

@Component
public class JwtHandler {

    private final String type = "Bearer ";

    // 토큰 생성
    public String createToken(String key, MemberJoinDTO memberJoinDTO, long maxAgeSeconds) {
        Date now = new Date();

        return type + Jwts.builder()
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + maxAgeSeconds * 1000L))
            .claim("memberId", memberJoinDTO.getMid())
            .claim("name", memberJoinDTO.getName())
            .signWith(SignatureAlgorithm.HS256, key.getBytes())
            .compact();
    }

    // 토큰의 유효성 검증
    public Optional<Claims> parse(String key, String token) { // 2
        try {
            return Optional.of(Jwts.parser().setSigningKey(key.getBytes()).parseClaimsJws(untype(token)).getBody());
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    private String untype(String token) {
        return token.substring(type.length());
    }

}
