package com.skypro.diploma.dto.user;

import com.skypro.diploma.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о пользователе")
public class UserDto {

    @Schema(description = "ID пользователя",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Имя пользователя (email)",
            example = "john@example.com")
    private String username;

    @Schema(description = "Имя пользователя",
            example = "John")
    private String firstName;

    @Schema(description = "Фамилия пользователя",
            example = "Doe")
    private String lastName;

    @Schema(description = "Номер телефона",
            example = "+7 999 123-45-67")
    private String phone;

    @Schema(description = "Роль пользователя",
            example = "USER")
    private Role role;

    @Schema(description = "URL аватара пользователя",
            example = "/images/users/1/avatar.jpg")
    private String image;
}
