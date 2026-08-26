package com.skypro.diploma.service;

import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.dto.user.NewPasswordReq;
import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.dto.user.UpdateUserReq;
import com.skypro.diploma.dto.user.UserDto;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ConflictException;
import com.skypro.diploma.exception.InvalidPasswordException;
import com.skypro.diploma.exception.NotFoundException;
import com.skypro.diploma.mapper.UserMapper;
import com.skypro.diploma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Сервис для работы с пользователями.
 *
 * Содержит ВСЮ бизнес-логику: проверка уникальности email,
 * шифрование пароля, сохранение в БД, обновление профиля,
 * сбор ответа для контроллера авторизации.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    /**
     * Возвращает ТЕКУЩЕГО авторизованного пользователя (Entity).
     * Spring Security после успешного входа кладет логин (email)
     * в SecurityContextHolder — оттуда мы его и достаем.
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Текущий пользователь не найден: " + username));
    }

    /**
     * Возвращает DTO текущего пользователя (для GET /users/me).
     */
    public UserDto getCurrentUserDto() {
        return userMapper.toDto(getCurrentUser());
    }

    /**
     * 🆕 Аутентифицирует пользователя и возвращает DTO с информацией о нём.
     * Используется контроллером /auth/login.
     *
     * Логика сбора ответа вынесена в сервис (замечание куратора),
     * чтобы контроллер оставался "тонким" — только HTTP-взаимодействие.
     *
     * @param username email пользователя
     * @return данные вошедшего пользователя (id, имя, роль)
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public LoginResp authenticateAndBuildResponse(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return new LoginResp(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    /**
     * Регистрирует нового пользователя (POST /auth/register).
     *
     * @throws ConflictException если email уже занят (→ 409)
     */
    @Transactional
    public void register(RegisterReq registerReq) {
        if (userRepository.existsByUsername(registerReq.getUsername())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        User user = userMapper.toEntity(registerReq);
        user.setPassword(passwordEncoder.encode(registerReq.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Зарегистрирован новый пользователь: {}", user.getUsername());
    }

    /**
     * Обновляет профиль текущего пользователя (PATCH /users/me).
     */
    @Transactional
    public UserDto updateCurrentUser(UpdateUserReq updateReq) {
        User user = getCurrentUser();

        userMapper.updateUserFromDto(updateReq, user);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Обновлен профиль пользователя: {}", user.getUsername());

        return userMapper.toDto(user);
    }

    /**
     * Меняет пароль текущего пользователя (POST /users/me/password).
     *
     * 🆕 При неверном текущем пароле бросает InvalidPasswordException (→ 400),
     * а не RuntimeException (→ 500). Это ожидаемая ошибка клиента.
     */
    @Transactional
    public void updatePassword(NewPasswordReq passwordReq) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(passwordReq.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Текущий пароль неверен");
        }

        user.setPassword(passwordEncoder.encode(passwordReq.getNewPassword()));
        userRepository.save(user);
        log.info("Обновлен пароль пользователя: {}", user.getUsername());
    }

    /**
     * Обновляет аватар текущего пользователя (PATCH /users/me/image).
     */
    @Transactional
    public String updateUserAvatar(MultipartFile image) throws IOException {
        User user = getCurrentUser();

        String imagePath = imageService.saveImage(image, "users", user.getId());

        user.setImagePath(imagePath);
        userRepository.save(user);
        log.info("Обновлена аватарка пользователя: {}", user.getUsername());

        return imagePath;
    }
}
