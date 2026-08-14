package com.skillgapai.config;

import com.skillgapai.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

/**
 * Phase 6e: every /api/** route now requires a valid JWT except
 * /api/auth/signup and /api/auth/login, which is how a client gets one
 * in the first place. Sessions are stateless - JwtAuthenticationFilter
 * (registered ahead of the filter that normally handles
 * {@link UsernamePasswordAuthenticationToken}) re-authenticates every
 * request from its bearer token; there's no server-side session to keep
 * in sync.
 *
 * CSRF is disabled: CSRF matters for cookie-based auth, where a
 * browser attaches credentials automatically; a bearer token only goes
 * out because our own frontend code puts it in the Authorization
 * header, which a cross-site page can't do.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated())
                // Without this, Spring Security's fallback entry point returns
                // 403 for a missing/invalid token too - technically wrong
                // (401 means "you're not authenticated", 403 means "you are,
                // but you're not allowed"). The frontend also relies on 401
                // specifically to know "send the user back to login".
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
