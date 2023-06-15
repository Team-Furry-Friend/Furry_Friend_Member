package com.v3.furry_friend_member.config;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.v3.furry_friend_member.repository.MemberRepository;
import com.v3.furry_friend_member.repository.TokenRepository;
import com.v3.furry_friend_member.security.CustomUserDetailService;
import com.v3.furry_friend_member.security.filter.JwtAuthenticationFilter;
import com.v3.furry_friend_member.security.filter.RefreshTokenFilter;
import com.v3.furry_friend_member.security.filter.TokenCheckFilter;
import com.v3.furry_friend_member.security.handler.Custom403Handler;
import com.v3.furry_friend_member.security.handler.CustomSocialLoginSuccessHandler;
import com.v3.furry_friend_member.security.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@RequiredArgsConstructor
// 특정 주소 접근시 권한 및 인증을 위한 어노테이션 활성화
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
// 필터 체인 관리 시작 어노테이션
@EnableWebSecurity
public class CustomSecurityConfig {

    private final DataSource dataSource;
    private final CustomUserDetailService customUserDetailService;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final TokenRepository tokenRepository;

    //자동로그인이 가능하도록 하는 기능을 이용해 DB 생성 및 자동 로그인 기능 구현.
    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
        return repo;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private TokenCheckFilter tokenCheckFilter(JwtUtil jwtUtil) {
        return new TokenCheckFilter(jwtUtil);
    }

    // 소셜 로그인 성공 후 처리를 위한 핸들러
    private CustomSocialLoginSuccessHandler customSocialLoginSuccessHandler() {
        return new CustomSocialLoginSuccessHandler(passwordEncoder(), jwtUtil, memberRepository, tokenRepository);
    }

    //403에러가 발생했을 때 이동시켜줄 페이지 설정
    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new Custom403Handler();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        /**
         * AuthenticationManagerBuilder
         * 인증 정보를 제공하는 userDetailsService와 비밀번호 인코딩을 위한 passwordEncoder를 설정하는 빌더 클래스
         */
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(customUserDetailService).passwordEncoder(passwordEncoder());
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        http.addFilterBefore(filter, CsrfFilter.class);

        http.authenticationManager(authenticationManager);

        /**
         * authenticationManager는 인증 처리를 위한 AuthenticationManager 객체를 의미
         * MemberDetailsService 클래스를 통해 유저 정보를 조회하고, 인증 처리를 수행
         * Spring Security를 사용할 경우 로그인 처리 로직을 직접 작성하지 않고,
         * Spring Security가 제공하는 로그인 기능을 사용
         */
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter("**/login");
        jwtAuthenticationFilter.setAuthenticationManager(authenticationManager);

        /**
         * MemberLoginFilter의 setAuthenticationSuccessHandler() 메서드를 호출
         * LoginSuccessHandler 객체를 등록함으로써 로그인 성공 시 LoginSuccessHandler 클래스의 onAuthenticationSuccess() 메서드가 호출되도록 설정
         */
        CustomSocialLoginSuccessHandler loginSuccessHandler = new CustomSocialLoginSuccessHandler(passwordEncoder(), jwtUtil, memberRepository, tokenRepository);
        jwtAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler);

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(tokenCheckFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(new RefreshTokenFilter("**/login", jwtUtil), TokenCheckFilter.class);

        // 소셜 로그인 처리를 위한 설정
        http.oauth2Login().loginPage("/member/login").successHandler(customSocialLoginSuccessHandler()); // 소셜 로그인 성공 후 처리를 담당하는 핸들러
        http.formLogin().loginProcessingUrl("/member/login").successHandler(customSocialLoginSuccessHandler());

        // 로그아웃
        http.logout().disable();

        http.csrf().disable();

        http.authorizeRequests().anyRequest().permitAll();

        http.rememberMe().key("12345678").tokenRepository(persistentTokenRepository())
                .userDetailsService(customUserDetailService).tokenValiditySeconds(60*60*24*30);

        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());

        return http.build();
    }

    //정적 파일 요청은 동작하지 않도록 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().requestMatchers(PathRequest.toStaticResources()
                .atCommonLocations());
    }

}
