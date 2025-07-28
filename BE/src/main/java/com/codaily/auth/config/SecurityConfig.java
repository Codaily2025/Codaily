package com.codaily.auth.config;

import com.codaily.auth.config.filter.JwtAuthenticationFilter;
import com.codaily.auth.config.filter.JwtAuthorizationFilter;
import com.codaily.auth.handler.CustomLogoutSuccessHandler;
import com.codaily.auth.handler.LoginSuccessHandler;
import com.codaily.auth.service.CustomUserDetailsServiceImpl;
import com.codaily.auth.service.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsServiceImpl customUserDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final CustomLogoutSuccessHandler logoutSuccessHandler;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {

        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenProvider);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/login");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/api/login", "/oauth/**", "/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .oauth2Login(oauth -> oauth
                        .successHandler(loginSuccessHandler) // JWT 발급 처리 핸들러
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                )
                .addFilter(jwtAuthenticationFilter)
                .addFilterBefore(
                        new JwtAuthorizationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // 실제 운영 시 반드시 변경!
    }
}


/**
 * 세션 로그인. 테스트에 사용했음.
 */
/*
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // 기본값이라 생략 가능
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/home","/", "/login", "/oauth/**", "/public/**").permitAll()
//                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
//                        .loginPage("/login")  // 커스텀 로그인 페이지 (필요 시)
//                        .defaultSuccessUrl("/home", true) // 로그인 성공 후 이동 페이지
                        .permitAll()
//                        .disable()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance(); // 프로덕션에서는 반드시 다른 인코더로!
    }
}*/
