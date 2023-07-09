package com.v3.furry_friend_member.filter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import com.v3.furry_friend_member.service.dto.CustomUserDetails;

public class CustomAuthenticationToken extends AbstractAuthenticationToken {

    private CustomUserDetails principal;

    public CustomAuthenticationToken(CustomUserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public CustomUserDetails getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        throw new UnsupportedOperationException();
    }

}