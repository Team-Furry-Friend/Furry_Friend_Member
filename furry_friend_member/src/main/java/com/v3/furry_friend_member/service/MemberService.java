package com.v3.furry_friend_member.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.v3.furry_friend_member.exception.AuthenticationEntryPointException;
import com.v3.furry_friend_member.exception.LoginFailureException;
import com.v3.furry_friend_member.exception.MemberEmailAlreadyExistsException;
import com.v3.furry_friend_member.service.dto.MemberJoinDTO;
import com.v3.furry_friend_member.entity.Member;
import com.v3.furry_friend_member.entity.MemberRole;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.service.dto.MemberLoginRequestDTO;
import com.v3.furry_friend_member.service.dto.MemberLoginResponseDTO;
import com.v3.furry_friend_member.service.dto.RefreshTokenResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    //회원가입
    public void join(MemberJoinDTO memberJoinDTO) throws Exception{

        // 이메일 및 닉네임 중복확인
        validateSignUpInfo(memberJoinDTO);
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
    }

    // 이메일 존재 확인
    private void validateSignUpInfo(MemberJoinDTO memberJoinDTO) {
        if(memberRepository.existsByEmail(memberJoinDTO.getEmail()))
            throw new MemberEmailAlreadyExistsException(memberJoinDTO.getEmail());
    }

    // 로그인 메서드
    public MemberLoginResponseDTO login(MemberLoginRequestDTO memberLoginRequestDTO) {

        // 기존의 유저 정보 검색
        Optional<Member> member = memberRepository.findByEmail(memberLoginRequestDTO.getUsername());

        if (member.isEmpty()) {
            // 사용자가 존재하지 않는 경우 예외 처리 또는 오류 메시지 반환
            throw new RuntimeException("User not found");
        }

        MemberJoinDTO memberJoinDTO = MemberJoinDTO.builder()
            .mid(member.get().getMid())
            .name(member.get().getName())
            .build();

        validatePassword(memberLoginRequestDTO, member.get());


        String accessToken = tokenService.createAccessToken(memberJoinDTO);
        String refreshToken = tokenService.createRefreshToken(memberJoinDTO);

        // 기존의 토큰이 있다면 재발급, 없다면 생성
        tokenService.saveToken(accessToken, refreshToken);

        return new MemberLoginResponseDTO(accessToken, refreshToken);
    }

    // 비밀번호 검증
    private void validatePassword(MemberLoginRequestDTO memberLoginRequestDTO, Member member) {
        if(!passwordEncoder.matches(memberLoginRequestDTO.getPassword(), member.getMpw())) {
            throw new LoginFailureException();
        }
    }

    // 사용자 찾기
    public String getMemberName(Long mid){
        Member member = memberRepository.findByMid(mid);

        return member.getName();
    }

    // 리프레시 토큰을 통해 리프레시 토큰 및 엑세스 토큰을 재발급
    public RefreshTokenResponse refreshToken(String rToken) {

        // 토큰 검증
        validateRefreshToken(rToken);
        // 데이터 추출
        MemberJoinDTO memberJoinDTO = tokenService.validateToken(rToken);

        //토큰 생성
        String accessToken = tokenService.createAccessToken(memberJoinDTO);
        String refreshToken = tokenService.createRefreshToken(memberJoinDTO);

        // 토큰 저장
        tokenService.saveToken(accessToken, refreshToken);

        return new RefreshTokenResponse(refreshToken, accessToken);
    }

    // 리프레시 토큰 검증
    private void validateRefreshToken(String rToken) {
        if(tokenService.validateToken(rToken).getMid() == null) {
            throw new AuthenticationEntryPointException();
        }
    }
}