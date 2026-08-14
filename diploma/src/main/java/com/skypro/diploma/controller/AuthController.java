package com.skypro.diploma.controller;

import com.skypro.diploma.dto.auth.LoginReq;
import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.dto.user.RegisterReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для авторизации и регистрации пользователей")
public class AuthController {

    @Operation(summary = "Авторизация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная авторизация",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResp.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
            @ApiResponse(responseCode = "400", description = "Неверный формат запроса")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResp> login(@Valid @RequestBody LoginReq loginReq) {
        // TODO: Этап III - реализовать аутентификацию
        LoginResp response = new LoginResp("dummy-jwt-token");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterReq registerReq) {
        // TODO: Этап III - реализовать регистрацию
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
