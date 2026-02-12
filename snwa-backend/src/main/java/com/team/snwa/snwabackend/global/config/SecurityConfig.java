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
        // allowCredentials가 true일 때는 와일드카드 사용 불가
        configuration.setAllowedOriginPatterns(List.of("*")); // 모든 origin 허용 (패턴 사용)
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
                        .requestMatchers("/api/auth/**").permitAll()  // 인증 관련 엔드포인트는 허용
                                .requestMatchers("/api/scheduler/**").permitAll()  // 요약 및 번역테스트용 스케줄러
                        .requestMatchers("/actuator/**").permitAll() // 모니터링용
                        .requestMatchers("/error").permitAll()  // 에러 페이지 허용
                        .requestMatchers("/api/notifications/subscribe").permitAll() // SSE는 토큰 쿼리로 처리
//                        .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 관리자 전용 실제 서비스용
                        .requestMatchers("/api/admin/**").permitAll() // 테스트용
                        //.requestMatchers("/api/orders/**","/api/coins/**","/api/payments/**").permitAll() //결제테스트용
                                .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exp/leaderboard").permitAll()
                        .anyRequest().authenticated()  // 나머지는 인증 필요
                );
        
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
