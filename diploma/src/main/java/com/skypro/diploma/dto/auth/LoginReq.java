package com.skypro.diploma.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на авторизацию пользователя")
public class LoginReq {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя (email)",
            example = "john@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль пользователя",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
