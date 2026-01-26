package com.team.snwa.snwabackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 임시 테스트용 SecurityConfig
 * 윤혁님 수정해주십쇼 ㄱ-
 *
 * @author 허준형
 * @DateOfCreated 2026-01-26
 * @DateOfEdit 2025-01-26
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/**").permitAll() // 모든 URL 허용
                );

        return http.build();
    }
}