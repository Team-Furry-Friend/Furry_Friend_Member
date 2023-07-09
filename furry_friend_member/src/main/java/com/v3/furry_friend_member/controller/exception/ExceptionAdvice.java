package com.v3.furry_friend_member.controller.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.v3.furry_friend_member.common.ApiResponse;
import com.v3.furry_friend_member.exception.AccessDeniedException;
import com.v3.furry_friend_member.exception.AuthenticationEntryPointException;
import com.v3.furry_friend_member.exception.LoginFailureException;
import com.v3.furry_friend_member.exception.MemberEmailAlreadyExistsException;

@RestControllerAdvice
@Slf4j
public class ExceptionAdvice {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse exception(Exception e) { // 1
        log.info("e = {}", e.getMessage());
        return ApiResponse.fail(-1000, "오류가 발생하였습니다.");
    }

    @ExceptionHandler(AuthenticationEntryPointException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse authenticationEntryPoint() {
        return ApiResponse.fail(-1001, "인증되지 않은 사용자입니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse accessDeniedException() {
        return ApiResponse.fail(-1002, "접근이 거부되었습니다.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse methodArgumentNotValidException(MethodArgumentNotValidException e) { // 2
        return ApiResponse.fail(-1003, e.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(LoginFailureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse loginFailureException() { // 3
        return ApiResponse.fail(-1004, "로그인에 실패하였습니다.");
    }

    @ExceptionHandler(MemberEmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse memberEmailAlreadyExistsException(MemberEmailAlreadyExistsException e) { // 4
        return ApiResponse.fail(-1005, e.getMessage() + "은 중복된 이메일 입니다.");
    }
}