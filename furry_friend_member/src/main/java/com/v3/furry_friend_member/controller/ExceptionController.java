package com.v3.furry_friend_member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v3.furry_friend_member.exception.AccessDeniedException;
import com.v3.furry_friend_member.exception.AuthenticationEntryPointException;

@RestController
public class ExceptionController {
    @GetMapping("/exception/entry-point")
    public void entryPoint() {
        throw new AuthenticationEntryPointException();
    }

    @GetMapping("/exception/access-denied")
    public void accessDenied() {
        throw new AccessDeniedException();
    }
}
