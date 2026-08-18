package com.skypro.diploma.repository;

import com.skypro.diploma.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Предоставляет стандартные CRUD-операции и кастомные методы поиска.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Найти пользователя по email (логину).
     * Используется при аутентификации и проверке уникальности email.
     *
     * @param username email пользователя
     * @return {@link Optional} с пользователем, если он найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверить, существует ли пользователь с таким email.
     * Используется при регистрации (чтобы не создать дубликат).
     *
     * @param username email пользователя
     * @return true, если пользователь с таким email уже есть
     */
    boolean existsByUsername(String username);
}
