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

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация Spring Security.
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ===== ОТКРЫТО ДЛЯ ВСЕХ (без авторизации) =====
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/v3/api-docs/**").permitAll()

                        // 🆕 Просмотр объявлений ОТКРЫТ — требование ТЗ
                        .requestMatchers(HttpMethod.GET, "/ads").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ads/{id}").permitAll()

                        // 🆕 Картинки объявлений и аватарки открыты (они нужны для отображения)
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()

                        // ===== ТРЕБУЕТ АВТОРИЗАЦИИ =====
                        // Создание/редактирование/удаление объявлений
                        .requestMatchers(HttpMethod.POST, "/ads").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/ads/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/ads/**").authenticated()

                        // Мои объявления
                        .requestMatchers(HttpMethod.GET, "/ads/me").authenticated()

                        // Комментарии (все операции требуют входа)
                        .requestMatchers("/ads/*/comments/**").authenticated()

                        // Профиль пользователя
                        .requestMatchers("/users/me/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
