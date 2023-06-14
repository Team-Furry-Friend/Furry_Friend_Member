package com.v3.furry_friend_member.security.filter;

import static com.v3.furry_friend_member.security.exception.AccessTokenException.TOKEN_ERROR.*;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.v3.furry_friend_member.security.exception.AccessTokenException;
import com.v3.furry_friend_member.security.util.JwtUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class TokenCheckFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Access Token을 검증하는 메서드
    private Map<String, Object> validateAccessToken(String accessToken) throws AccessTokenException {

        if (accessToken == null) {
            // Authorization 헤더의 값이 null 일경우,
            // 유효한 JWT 토큰이 아니므로 AccessTokenException 예외를 발생
            // - Authorization 헤더의 값은 "JWT Token" 형식으로 구성
            throw new AccessTokenException(UNACCEPT);
        }

        try {
            Map<String, Object> values = jwtUtil.validateToken(accessToken);
            return values;
        } catch (MalformedJwtException malformedJwtException) {
            log.error("🚨 MalformedJwtException -------------------- 🚨");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.MALFORM);
        }catch(SignatureException signatureException){
            log.error("🚨 SignatureException -------------------- 🚨");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.BADSIGN);
        }catch(ExpiredJwtException expiredJwtException){
            log.error("🚨 ExpiredJwtException -------------------- 🚨");
            throw new AccessTokenException(AccessTokenException.TOKEN_ERROR.EXPIRED);
        }
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws IOException, ServletException {
        String path = request.getRequestURI();
        /**
         * [1] 요청 경로가 모든 경로일 때 필터링을 수행
         * [2] Request Header에서 "Authorization" 헤더를 찾아 JWT 토큰을 추출
         * [3] 추출한 JWT 토큰의 유효성을 검사
         * [4] 토큰이 유효한 경우, 추출한 토큰에서 사용자 정보를 추출하여 SecurityContext에 저장
         * [5] 유효한 토큰이 아닌 경우, 401 Unauthorized 응답을 보냅니다.
         */
        if (path.startsWith("/")) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("🛠️ Token Check Filter -------------------- 🛠️");
        log.info("💡 JWTUtil =====> " + jwtUtil);

        Cookie[] cookies = request.getCookies();
        String accessToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("access_token")) {
                    accessToken = cookie.getValue();
                    // access_token을 사용하여 원하는 작업 수행
                    break;
                }
            }
        }

        try {
            if (accessToken == null) {
                // accessToken이 null일 때 예외 처리
                throw new AccessTokenException(UNACCEPT);
            }

            validateAccessToken(accessToken);
            filterChain.doFilter(request, response);
        } catch (AccessTokenException accessTokenException) {
            accessTokenException.sendResponseError(response);
        }

    }
}