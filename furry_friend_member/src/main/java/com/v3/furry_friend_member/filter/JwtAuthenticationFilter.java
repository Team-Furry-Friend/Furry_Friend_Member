package com.v3.furry_friend_member.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.v3.furry_friend_member.service.CustomUserDetailsService;
import com.v3.furry_friend_member.service.TokenService;
import com.v3.furry_friend_member.service.dto.CustomUserDetails;
import com.v3.furry_friend_member.service.dto.MemberJoinDTO;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final TokenService tokenService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String token = extractToken(request);

        // 엑세스 토큰이 유효할 때만 저장
        if(validateToken(token)) {
            setAuthentication(token);
        }

        chain.doFilter(request, response);
    }

    private String extractToken(ServletRequest request) {
        return ((HttpServletRequest)request).getHeader("Authorization");
    }

    private boolean validateToken(String token) {
        return token != null && tokenService.validateToken(token).getMid() != null;
    }

    private void setAuthentication(String token) {

        MemberJoinDTO memberJoinDTO = tokenService.validateToken(token);
        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(Long.toString(memberJoinDTO.getMid()));
        SecurityContextHolder.getContext().setAuthentication(new CustomAuthenticationToken(userDetails, userDetails.getAuthorities()));
    }
}
