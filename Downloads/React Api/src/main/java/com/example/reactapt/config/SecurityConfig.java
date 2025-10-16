package com.example.reactapt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())                  // POST 테스트 편의
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()      // ✅ API 전체 허용
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults())          // 기본 인증 유지(무시됨)
                .build();
    }
}
