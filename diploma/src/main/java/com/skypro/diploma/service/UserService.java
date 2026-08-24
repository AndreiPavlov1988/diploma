package com.skypro.diploma.service;

import com.skypro.diploma.dto.user.NewPasswordReq;
import com.skypro.diploma.dto.user.RegisterReq;
import com.skypro.diploma.dto.user.UpdateUserReq;
import com.skypro.diploma.dto.user.UserDto;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ConflictException;
import com.skypro.diploma.exception.NotFoundException;
import com.skypro.diploma.mapper.UserMapper;
import com.skypro.diploma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * шифрование пароля, сохранение в БД, обновление профиля.
 * Контроллеры только вызывают методы этого сервиса.
 */
@Slf4j
@Service
@RequiredArgsConstructor  // Lombok создает конструктор со всеми final-полями
public class UserService {

    private final UserRepository userRepository;   // доступ к таблице users
    private final UserMapper userMapper;           // преобразование Entity <-> DTO
    private final PasswordEncoder passwordEncoder; // BCrypt из SecurityConfig
    private final ImageService imageService;       // сохранение аватарок

    /**
     * Возвращает ТЕКУЩЕГО авторизованного пользователя (Entity).
     *
     * Spring Security после успешного входа кладет логин (email)
     * в SecurityContextHolder — оттуда мы его и достаем.
     *
     * @return пользователь из БД
     * @throws NotFoundException если пользователя нет в БД (→ 404)
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();  // email из заголовка Authorization
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
     * Регистрирует нового пользователя (POST /auth/register).
     *
     * @param registerReq данные из формы регистрации
     * @throws ConflictException если email уже занят (→ 409)
     */
    @Transactional  // все операции выполнятся в одной транзакции
    public void register(RegisterReq registerReq) {
        // 1. Проверяем уникальность email (требование OpenAPI: 409 Conflict)
        if (userRepository.existsByUsername(registerReq.getUsername())) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }

        // 2. DTO -> Entity через MapStruct
        User user = userMapper.toEntity(registerReq);

        // 3. ШИФРУЕМ пароль через BCrypt (в БД никогда не хранится открытый пароль!)
        user.setPassword(passwordEncoder.encode(registerReq.getPassword()));

        // 4. Заполняем поля, которые не пришли из DTO
        if (user.getRole() == null) {
            user.setRole(Role.USER);  // роль по умолчанию — USER
        }
        user.setActive(true);                       // аккаунт активен
        user.setCreatedAt(LocalDateTime.now());     // дата создания
        user.setUpdatedAt(LocalDateTime.now());     // дата обновления

        // 5. Сохраняем в БД
        userRepository.save(user);
        log.info("Зарегистрирован новый пользователь: {}", user.getUsername());
    }

    /**
     * Обновляет профиль текущего пользователя (PATCH /users/me).
     *
     * @param updateReq новые имя/фамилия/телефон
     * @return обновленный DTO
     */
    @Transactional
    public UserDto updateCurrentUser(UpdateUserReq updateReq) {
        User user = getCurrentUser();

        // MapStruct копирует firstName/lastName/phone из DTO в Entity
        userMapper.updateUserFromDto(updateReq, user);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Обновлен профиль пользователя: {}", user.getUsername());

        return userMapper.toDto(user);
    }

    /**
     * Меняет пароль текущего пользователя (POST /users/me/password).
     *
     * @param passwordReq текущий и новый пароль
     * @throws RuntimeException если текущий пароль неверный (→ 400 через обработчик)
     */
    @Transactional
    public void updatePassword(NewPasswordReq passwordReq) {
        User user = getCurrentUser();

        // Проверяем, что пользователь правильно ввел ТЕКУЩИЙ пароль
        // passwordEncoder.matches(открытый, хэш) — безопасное сравнение
        if (!passwordEncoder.matches(passwordReq.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Текущий пароль неверен");
        }

        // Шифруем НОВЫЙ пароль и сохраняем
        user.setPassword(passwordEncoder.encode(passwordReq.getNewPassword()));
        userRepository.save(user);
        log.info("Обновлен пароль пользователя: {}", user.getUsername());
    }

    /**
     * Обновляет аватар текущего пользователя (PATCH /users/me/image).
     *
     * @param image загруженный файл
     * @return URL новой аватарки
     */
    @Transactional
    public String updateUserAvatar(MultipartFile image) throws IOException {
        User user = getCurrentUser();

        // Сохраняем файл на диск через ImageService
        String imagePath = imageService.saveImage(image, "users", user.getId());

        // В БД храним только путь
        user.setImagePath(imagePath);
        userRepository.save(user);
        log.info("Обновлена аватарка пользователя: {}", user.getUsername());

        return imagePath;
    }
}
