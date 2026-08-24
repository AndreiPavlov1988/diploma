package com.skypro.diploma.controller;

import com.skypro.diploma.dto.auth.LoginReq;
import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Контроллер аутентификации: регистрация и вход.
 *
 * НЕ имеет @RequestMapping, поэтому URL идут без префикса:
 * - POST /auth/login   → login()
 * - POST /auth/register → register()
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для авторизации и регистрации пользователей")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    /**
     * Авторизация пользователя (POST /auth/login).
     *
     * AuthenticationManager проверит email и пароль через UserDetailsServiceImpl.
     * Если неверные → автоматически вернется 401 Unauthorized (ловит GlobalExceptionHandler).
     */
    @Operation(summary = "Авторизация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "400", description = "Неверный формат запроса")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResp> login(@Valid @RequestBody LoginReq loginReq) {
        // 1. Проверяем логин/пароль через Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginReq.getUsername(),
                        loginReq.getPassword()
                )
        );

        // 2. Возвращаем токен (для фронтенда — заглушка, так как он использует Basic Auth)
        return ResponseEntity.ok(new LoginResp(UUID.randomUUID().toString()));
    }

    /**
     * Регистрация нового пользователя (POST /auth/register).
     *
     * Если email уже занят → ConflictException → 409 Conflict.
     */
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping("/auth/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterReq registerReq) {
        userService.register(registerReq);  // вся логика в сервисе
        return ResponseEntity.status(HttpStatus.CREATED).build();  // код 201
    }
}
