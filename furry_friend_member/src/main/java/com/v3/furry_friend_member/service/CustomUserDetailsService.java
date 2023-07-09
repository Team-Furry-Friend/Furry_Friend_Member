package com.v3.furry_friend_member.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.v3.furry_friend_member.entity.Member;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.service.dto.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Optional<Member> member = memberRepository.findById(Long.valueOf(userId));
        return new CustomUserDetails(
            String.valueOf(member.get().getMid()),
            member.get().getRoleSet().stream()
                .map(roleType -> roleType.toString())
                .map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
        );
    }
}