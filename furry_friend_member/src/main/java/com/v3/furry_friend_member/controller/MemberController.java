package com.v3.furry_friend_member.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v3.furry_friend_member.common.ApiResponse;
import com.v3.furry_friend_member.service.dto.JwtRequest;
import com.v3.furry_friend_member.service.dto.MemberJoinDTO;
import com.v3.furry_friend_member.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/member")
//member와 관련된 요청을 처리할 메서드
public class MemberController {

    private final MemberService memberService;

    //회원 가입 처리
    @PostMapping("/join")
    public ApiResponse join(@RequestBody MemberJoinDTO memberJoinDTO){

        log.info("memberJoinDTO : " + memberJoinDTO);
        try{

            // 성공
            memberService.join(memberJoinDTO);
            return ApiResponse.success("회원가입 성공");
        } catch (Exception e) {
            
            // 실패
            log.error("회원가입 실패: " + e.getMessage());
            return ApiResponse.fail(400, "회원가입 실패");
        }
    }

    // 로그아웃 후 메인 페이지로 이동
    @PostMapping("/logout")
    public ApiResponse logout(@RequestBody JwtRequest jwtRequest){
        try{
            memberService.logout(jwtRequest.getAccess_token());

            return ApiResponse.success("로그아웃 성공");
        } catch(IllegalArgumentException e){

            log.error("토큰이 존재하지 않습니다.", e);
            return ApiResponse.fail(400, "로그아웃 실패");
        }
    }
}