package com.skypro.diploma.service;

import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.dto.user.NewPasswordReq;
import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ConflictException;
import com.skypro.diploma.exception.InvalidPasswordException;
import com.skypro.diploma.mapper.UserMapper;
import com.skypro.diploma.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты UserService (без Spring-контекста и БД).
 * @Mock — создаёт поддельную зависимость, @InjectMocks — подставляет моки в сервис.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserService userService;

    @AfterEach
    void tearDown() {
        // очищаем "вошедшего пользователя" после каждого теста
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_whenEmailExists_throwsConflict() {
        RegisterReq req = new RegisterReq();
        req.setUsername("test@mail.ru");
        when(userRepository.existsByUsername("test@mail.ru")).thenReturn(true);

        // email занят -> ConflictException (в API это 409)
        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(ConflictException.class);

        // и пользователь НЕ сохраняется
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_success_savesUserWithEncodedPasswordAndDefaultRole() {
        RegisterReq req = new RegisterReq();
        req.setUsername("test@mail.ru");
        req.setPassword("rawPassword");

        User entity = new User();
        when(userRepository.existsByUsername("test@mail.ru")).thenReturn(false);
        when(userMapper.toEntity(req)).thenReturn(entity);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashed");

        userService.register(req);

        // пароль в БД — только хэш, роль по умолчанию USER, аккаунт активен
        assertThat(entity.getPassword()).isEqualTo("hashed");
        assertThat(entity.getRole()).isEqualTo(Role.USER);
        assertThat(entity.isActive()).isTrue();
        verify(userRepository).save(entity);
    }

    @Test
    void updatePassword_whenCurrentPasswordWrong_throwsInvalidPassword() {
        // имитируем вошедшего пользователя
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@mail.ru", null));

        User user = new User();
        user.setUsername("test@mail.ru");
        user.setPassword("hashedOld");
        when(userRepository.findByUsername("test@mail.ru")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedOld")).thenReturn(false);

        NewPasswordReq req = new NewPasswordReq();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newPassword");

        // неверный текущий пароль -> InvalidPasswordException (в API это 400, не 500!)
        assertThatThrownBy(() -> userService.updatePassword(req))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void authenticateAndBuildResponse_returnsUserData() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test@mail.ru");
        user.setFirstName("Иван");
        user.setLastName("Иванов");
        user.setRole(Role.USER);
        when(userRepository.findByUsername("test@mail.ru")).thenReturn(Optional.of(user));

        LoginResp resp = userService.authenticateAndBuildResponse("test@mail.ru");

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getUsername()).isEqualTo("test@mail.ru");
        assertThat(resp.getFirstName()).isEqualTo("Иван");
        assertThat(resp.getRole()).isEqualTo("USER");
    }
}
