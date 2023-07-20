package com.v3.furry_friend_member.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.v3.furry_friend_member.filter.JwtAuthenticationFilter;
import com.v3.furry_friend_member.handler.CustomAccessDeniedHandler;
import com.v3.furry_friend_member.handler.CustomAuthenticationEntryPoint;
import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.service.CustomUserDetailsService;
import com.v3.furry_friend_member.service.TokenService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EnableWebSecurity // 필터를 필터체인에 등록
public class CustomSecurityConfig extends WebSecurityConfigurerAdapter {

    private final CustomUserDetailsService customUserDetailsService;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception{

        http
            .httpBasic().disable()
            .formLogin().disable()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // session 사용 x
            .and()
            .authorizeRequests()
                .anyRequest().permitAll()
            .and()
                .exceptionHandling().accessDeniedHandler(new CustomAccessDeniedHandler())
            .and()
                .exceptionHandling().authenticationEntryPoint(new CustomAuthenticationEntryPoint())
            .and()
                .addFilterBefore(new JwtAuthenticationFilter(tokenService, customUserDetailsService), UsernamePasswordAuthenticationFilter.class);
    }

    //정적 파일 요청은 동작하지 않도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources()
                .atCommonLocations());
    }

}
