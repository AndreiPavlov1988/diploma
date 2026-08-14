package com.skypro.diploma.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на смену пароля")
public class NewPasswordReq {

    @NotBlank(message = "Текущий пароль не может быть пустым")
    @Schema(description = "Текущий пароль",
            example = "oldPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Schema(description = "Новый пароль",
            example = "newPassword123",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
