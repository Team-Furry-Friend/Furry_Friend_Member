package com.v3.furry_friend_member.security.util;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JwtUtil {

    @Value("${jwt.secretKey}")
    private String key;

    public String generateToken(int minutes, Long memberId) {

        Claims claims = Jwts.claims();
        claims.put("memberId", memberId);

        String jwtStr = Jwts.builder()
            .claim("memberId", memberId)
            .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
            .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(minutes).toInstant()))
            .signWith(SignatureAlgorithm.HS256, key)
            .compact();
        return jwtStr;
    }

    // 토큰의 유효성 검사 메서드
    public Map<String, Object> validateToken(String token)throws JwtException {

        Map<String, Object> claim = null;
        log.warn(token);
        claim = Jwts.parser()
            .setSigningKey(key) // Set Key
            .parseClaimsJws(token) // 파싱 및 검증, 실패시 에러
            .getBody();
        return claim;
    }

}