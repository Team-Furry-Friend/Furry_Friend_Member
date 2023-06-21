package com.v3.furry_friend_member.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.v3.furry_friend_member.service.dto.MemberJoinDTO;
import com.v3.furry_friend_member.entity.Member;
import com.v3.furry_friend_member.entity.MemberRole;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.repository.TokenRepository;
import com.v3.furry_friend_member.service.dto.MemberSecurityDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class MemberService {

    @Value("${jwt.secretKey}")
    private String key;

    //회원이 존재하는 경우 발생시킬 예외 클래스
    static class MidExistException extends Exception{

    }

    private final MemberRepository memberRepository;

    private final TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, TokenRepository tokenRepository) {

        // 기본 생성자
        this.memberRepository = memberRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    //회원가입
    public void join(MemberJoinDTO memberJoinDTO) throws Exception{
        //이메일 중복 확인
        String email = memberJoinDTO.getEmail();

        boolean exist = memberRepository.existsByEmail(email);
        if(!exist){
            //회원 가입을 위해서 입력 받은 정보를 가지고 Member Entity를 생성
            Member member = Member.builder()
                .mpw(memberJoinDTO.getMpw())
                .email(memberJoinDTO.getEmail())
                .name(memberJoinDTO.getName())
                .address(memberJoinDTO.getAddress())
                .phone(memberJoinDTO.getPhone())
                .del(memberJoinDTO.isDel())
                .social(memberJoinDTO.isSocial())
                .build();

            //비밀번호 암호화
            member.changePassword(passwordEncoder.encode(memberJoinDTO.getMpw()));
            //권한 설정
            member.addRole(MemberRole.USER);

            memberRepository.save(member);
        }else{
            throw new Exception("중복된 이메일입니다.");
        }
    }

    // 토큰의 유효성 검사 및 member 고유 번호 찾아오는 메서드
    public Long getMemberId(String access_token){

        try {
            // accessToken 파싱 및 memberId 추출
            Claims claims = Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(access_token)
                .getBody();

            Object object = claims.get("memberId"); // memberId 추출
            Long memberId = null;
            if (object instanceof Long) {
                memberId = (Long) object;
            } else if (object instanceof Integer) {
                memberId = ((Integer) object).longValue();
            } else {
                log.warn(claims.get("memberId"));
            }

            return memberId;
        } catch (SignatureException e) {
            // 토큰이 유효하지 않으면 false를 반환
            log.error("SignatureException: " + e);
            return null;
        }
    }

    // 로그아웃(DB 토큰 및 쿠키 토큰 삭제)
    public void logout(String access_token){
        tokenRepository.deleteTokenByUserId(getMemberId(access_token));
    }

    // 사용자 찾기
    public String getMemberName(Long mid){
        Member member = memberRepository.findByMid(mid);

        return member.getName();
    }
}