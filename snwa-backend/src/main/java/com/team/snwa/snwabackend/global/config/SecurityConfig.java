package com.team.snwa.snwabackend.global.config;

import com.team.snwa.snwabackend.global.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                // 실제 운영 환경의 프론트엔드 도메인과 로컬 개발 환경만 허용하도록 제한합니다.
                configuration.setAllowedOriginPatterns(
                                Arrays.asList("http://localhost:5173", "https://*.surge.sh", "https://*.vercel.app","http://3.39.237.24:70"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization"));
                configuration.setMaxAge(3600L); // Preflight 요청 캐시 시간

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**").permitAll() // 인증 관련 엔드포인트 허용
                                                .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll() // 시스템
                                                                                                                         // 필수
                                                                                                                         // 모니터링만
                                                                                                                         // 허용
                                                .requestMatchers("/actuator/**").hasRole("ADMIN") // 나머지는 관리자 전용
                                                .requestMatchers("/api/notifications/subscribe").permitAll() // SSE 토큰
                                                                                                             // 쿼리 처리
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 관리자 전용 서비스 보호
                                                .requestMatchers("/api/scheduler/**").hasRole("ADMIN") // 스케줄러 수동 조작 관리자
                                                                                                       // 전용
                                                .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**")
                                                .permitAll() // 기사 조회 허용
                                                .requestMatchers(HttpMethod.GET, "/api/exp/leaderboard").permitAll() // 랭킹
                                                                                                                     // 조회
                                                                                                                     // 허용
                                                .requestMatchers(HttpMethod.POST, "/api/articles/crawl",
                                                                "/api/articles/crawl/**")
                                                .hasRole("ADMIN") // 크롤링 트리거 관리자 전용 보호
                                                .anyRequest().authenticated() // 나머지는 인증 필요
                                );

                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
