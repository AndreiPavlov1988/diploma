package com.skypro.diploma.controller;

import com.skypro.diploma.dto.auth.LoginReq;
import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер аутентификации: регистрация и вход.
 *
 * НЕ имеет @RequestMapping, поэтому URL прописаны полностью:
 * - POST /auth/login    → login()
 * - POST /auth/register → register()
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для авторизации и регистрации пользователей")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    /**
     * Авторизация пользователя (POST /auth/login).
     *
     * Приложение использует Basic Auth, поэтому JWT-токен НЕ нужен.
     * Вместо фиктивного токена возвращаем данные вошедшего пользователя —
     * фронтенд сразу получает id, имя и роль для отображения профиля.
     *
     * Если логин или пароль неверные — authenticationManager бросит
     * BadCredentialsException, которую GlobalExceptionHandler превратит в 401.
     */
    @Operation(summary = "Авторизация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "400", description = "Неверный формат запроса")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResp> login(@Valid @RequestBody LoginReq loginReq) {
        // 1. Проверяем логин и пароль через Spring Security.
        //    Spring сам найдет пользователя через UserDetailsServiceImpl
        //    и сравнит BCrypt-хэш пароля.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginReq.getUsername(),
                        loginReq.getPassword()
                )
        );

        // 2. Если дошли до сюда — пароль верный. Достаем пользователя из БД.
        User user = userRepository.findByUsername(loginReq.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        // 3. Собираем ответ с данными пользователя (вместо фиктивного токена)
        LoginResp response = new LoginResp(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Регистрация нового пользователя (POST /auth/register).
     *
     * Если email уже занят — UserService бросит ConflictException → 409.
     */
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping("/auth/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterReq registerReq) {
        userService.register(registerReq);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
