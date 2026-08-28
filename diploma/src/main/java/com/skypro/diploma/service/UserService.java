package com.skypro.diploma.service;

import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.dto.user.NewPasswordReq;
import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.dto.user.UpdateUserReq;
import com.skypro.diploma.dto.user.UserDto;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ConflictException;
import com.skypro.diploma.exception.InvalidImageException;
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
 * Сервис для работы с пользователями: регистрация, профиль,
 * смена пароля, аватар, сбор ответа авторизации.
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
     * Логин берётся из SecurityContextHolder после успешного входа.
     *
     * @return сущность текущего пользователя
     * @throws NotFoundException если пользователь не найден в БД
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
     *
     * @return DTO профиля
     */
    public UserDto getCurrentUserDto() {
        return userMapper.toDto(getCurrentUser());
    }

    /**
     * Собирает ответ для успешной авторизации (POST /auth/login).
     * Логика вынесена из контроллера — контроллер остаётся «тонким».
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
     * Пароль сохраняется только в виде BCrypt-хэша, роль по умолчанию — USER.
     *
     * @param registerReq данные регистрации
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
     *
     * @param updateReq новые данные профиля
     * @return обновлённое DTO профиля
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
     * @param passwordReq текущий и новый пароли
     * @throws InvalidPasswordException если текущий пароль неверен (→ 400)
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
     *
     * 🆕 После успешной замены СТАРАЯ картинка удаляется с диска,
     * чтобы не накапливать неиспользуемые файлы (замечание наставника).
     *
     * @param image новый файл аватара
     * @return URL-путь новой картинки
     * @throws IOException           если не удалось записать файл
     * @throws InvalidImageException если файл не является картинкой (→ 400)
     */
    @Transactional
    public String updateUserAvatar(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Файл изображения пуст");
        }

        User user = getCurrentUser();
        String oldPath = user.getImagePath();

        String imagePath = imageService.saveImage(image, "users", user.getId());
        user.setImagePath(imagePath);
        userRepository.save(user);

        // Удаляем старый файл ПОСЛЕ успешного сохранения нового
        imageService.deleteImage(oldPath);

        log.info("Обновлена аватарка пользователя: {}", user.getUsername());
        return imagePath;
    }
}
