package com.skypro.diploma.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация Spring Security + CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ===== ОТКРЫТО ДЛЯ ВСЕХ =====
                        // 🆕 Оба варианта адресов: с /auth и без (для фронтенда)
                        .requestMatchers("/auth/login", "/auth/register", "/login", "/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ads").permitAll()

                        // ВАЖНО: /ads/me ПЕРЕД /ads/{id}
                        .requestMatchers(HttpMethod.GET, "/ads/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/ads/{id}").permitAll()

                        // ===== ТРЕБУЕТ АВТОРИЗАЦИИ =====
                        .requestMatchers(HttpMethod.POST, "/ads").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/ads/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/ads/**").authenticated()
                        .requestMatchers("/ads/*/comments/**").authenticated()
                        .requestMatchers("/users/me/**").authenticated()

                        .anyRequest().authenticated()
                )
                // Basic Auth с игнорированием неудачи на публичных эндпоинтах
                .addFilterBefore(new BasicAuthenticationFilter(authenticationManager),
                        BasicAuthenticationFilter.class)
                // 401 для защищённых эндпоинтов без валидной аутентификации
                .exceptionHandling(e -> e.authenticationEntryPoint(new BasicAuthenticationEntryPoint()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
