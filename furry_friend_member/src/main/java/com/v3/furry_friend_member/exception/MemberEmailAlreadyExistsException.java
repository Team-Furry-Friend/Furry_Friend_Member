package com.v3.furry_friend_member.exception;

public class MemberEmailAlreadyExistsException extends RuntimeException {
    public MemberEmailAlreadyExistsException(String message) {
        super(message);
    }
}