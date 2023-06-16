package com.v3.furry_friend_member.security.handler;

import java.io.IOException;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.v3.furry_friend_member.common.ApiResponse;
import com.v3.furry_friend_member.entity.Member;
import com.v3.furry_friend_member.entity.Token;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.repository.TokenRepository;
import com.v3.furry_friend_member.security.util.JwtUtil;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
//스프링 시큐리티 중 로그인 성공을 하고 이후의 작업을 커스터 마이징한 클래스
public class CustomSocialLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;

    /**
     * HttpServletRequest 객체 - 로그인 요청 정보를 담고 있음
     * HttpServletResponse 객체 - 로그인 응답 정보를 담고 있음
     * Authentication 객체 - 인증에 성공한 사용자 정보를 담고 있음.
     */

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication) throws IOException {

        /**
         * onAuthenticationSuccess() 메소드에서 response.setContentType()을 통해
         * 로그인 성공 시 전송되는 응답 정보의 ContentType을 JSON으로 설정
         */
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        log.info("💡 authentication =====> " + authentication);
        log.info("💡 LOGINID =====> " + authentication.getName());

        // Member 엔티티를 조회합니다. 로그인한 사용자의 ID를 기준으로 조회합니다.
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("등록된 아이디가 없습니다."));

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // AccessToken 유효기간 30분
        String accessToken = jwtUtil.generateToken(30, member.getMid());
        // RefreshToken 유효기간 7일
        String refreshToken = jwtUtil.generateToken(7 * 24 * 60, member.getMid());

        log.info("accessToken: " + accessToken);
        log.info("refreshToken: " + refreshToken);

        // Member 엔티티에 RefreshToken 값을 저장합니다.
        Token token = Token.builder()
            .userId(member.getMid())
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();

        tokenRepository.save(token);

        // 로그인 성공 메시지
        ApiResponse<String> apiResponse = ApiResponse.success("success", accessToken);

        // Set the response status and write the JSON response to the output
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(apiResponse.toJson());
    }
}
