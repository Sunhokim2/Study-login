package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;


//스프링부트 시큐리티 때문에 예외처리를 해줘야함
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable()) // CSRF 비활성화 (운영 환경은 끄기)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/send-verification-code").permitAll()
                        .requestMatchers("/api/verify-code").permitAll()
                        .requestMatchers("/api/verify-email").permitAll()
                        .requestMatchers("/api/register").permitAll()
                        .requestMatchers("/api/login").permitAll()
                        .requestMatchers("/api/users").authenticated() // /api/users는 인증된 사용자만 접근 가
                        .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
                );
        return http.build();
    }
}