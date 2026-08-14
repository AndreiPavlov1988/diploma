package com.skypro.diploma.controller;

import com.skypro.diploma.dto.user.NewPasswordReq;
import com.skypro.diploma.dto.user.UpdateUserReq;
import com.skypro.diploma.dto.user.UserDto;
import com.skypro.diploma.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "API для управления профилем пользователя")
public class UserController {

    @Operation(summary = "Получить информацию о текущем пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация получена успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        // TODO: Этап III - получить пользователя из SecurityContextHolder
        return ResponseEntity.ok(createTestUserDto());
    }

    @Operation(summary = "Обновить информацию о пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные обновлены успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(@Valid @RequestBody UpdateUserReq updateReq) {
        // TODO: Этап III - обновить пользователя
        UserDto userDto = createTestUserDto();
        userDto.setFirstName(updateReq.getFirstName());
        userDto.setLastName(updateReq.getLastName());
        userDto.setPhone(updateReq.getPhone());
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Обновить пароль пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пароль обновлен успешно"),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@Valid @RequestBody NewPasswordReq passwordReq) {
        // TODO: Этап III - обновить пароль
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновить аватар пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Аватар обновлен успешно"),
            @ApiResponse(responseCode = "400", description = "Неверный формат файла"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateUserAvatar(@RequestParam("image") MultipartFile image) {
        // TODO: Этап IV - сохранить изображение
        return ResponseEntity.ok("/images/users/1/avatar.jpg");
    }

    private UserDto createTestUserDto() {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("john@example.com");
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setPhone("+7 999 123-45-67");
        userDto.setImage("/images/users/1/avatar.jpg");
        userDto.setRole(Role.USER);
        return userDto;
    }
}
