package com.skypro.diploma.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ответ при успешной авторизации.
 *
 * Приложение использует Basic Auth (каждый запрос содержит Authorization header),
 * поэтому JWT-токен не нужен. Вместо токена возвращаем информацию о пользователе —
 * это позволяет фронтенду сразу отобразить профиль после входа без дополнительного запроса.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ при успешной авторизации")
public class LoginResp {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @Schema(description = "Email пользователя", example = "john@example.com")
    private String username;

    @Schema(description = "Имя пользователя", example = "Иван")
    private String firstName;

    @Schema(description = "Фамилия пользователя", example = "Иванов")
    private String lastName;

    @Schema(description = "Роль пользователя", example = "USER")
    private String role;
}
