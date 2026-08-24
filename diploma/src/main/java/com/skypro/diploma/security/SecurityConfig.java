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
 * Конфигурация Spring Security (требование ТЗ Этапа III).
 *
 * Настраивает:
 * 1. Какие эндпоинты открыты без входа, а какие требуют авторизации
 * 2. Basic-аутентификацию (её использует фронтенд)
 * 3. Шифрование паролей алгоритмом BCrypt
 * 4. Отключение сессий (REST API должен быть stateless)
 */
@Configuration          // класс-конфигурация Spring
@EnableWebSecurity      // включает Spring Security
@EnableMethodSecurity   // включает аннотацию @PreAuthorize для проверки ролей в методах
public class SecurityConfig {

    /**
     * Шифровальщик паролей.
     * BCrypt — индустриальный стандарт: пароль хранится в БД в виде необратимого хэша.
     * Используется при регистрации (шифруем) и при входе (сравниваем).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Менеджер аутентификации.
     * Нужен контроллеру /login, чтобы программно проверить логин и пароль.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Цепочка фильтров безопасности — ГЛАВНЫЙ метод конфигурации.
     * Определяет правила доступа ко всем эндпоинтам приложения.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Отключаем CSRF-защиту: она нужна для сайтов с сессиями,
                //    а у нас REST API с Basic Auth — CSRF не нужен
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Настраиваем правила доступа к эндпоинтам
                .authorizeHttpRequests(auth -> auth
                        // ===== ОТКРЫТО ДЛЯ ВСЕХ (без входа) =====
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/api-docs/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()

                        // ===== ВСЁ ОСТАЛЬНОЕ — ТОЛЬКО ДЛЯ АВТОРИЗОВАННЫХ =====
                        .anyRequest().authenticated()
                )

                // 3. Включаем Basic-аутентификацию (логин:пароль в заголовке Authorization)
                //    Именно её использует фронтенд согласно ТЗ
                .httpBasic(withDefaults())

                // 4. Отключаем сессии: каждый запрос проверяется заново (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
