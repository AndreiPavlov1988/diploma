package com.skypro.diploma.dto.user;

import com.skypro.diploma.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на регистрацию нового пользователя")
public class RegisterReq {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Schema(description = "Имя пользователя (email)",
            example = "john@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Schema(description = "Пароль",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "Имя не может быть пустым")
    @Schema(description = "Имя пользователя",
            example = "John",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Schema(description = "Фамилия пользователя",
            example = "Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "Телефон не может быть пустым")
    @Pattern(regexp = "^\\+?[0-9\\s-]{10,15}$",
            message = "Неверный формат телефона")
    @Schema(description = "Номер телефона",
            example = "+7 999 123-45-67",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    @Schema(description = "Роль пользователя",
            example = "USER",
            defaultValue = "USER")
    private Role role = Role.USER;
}
