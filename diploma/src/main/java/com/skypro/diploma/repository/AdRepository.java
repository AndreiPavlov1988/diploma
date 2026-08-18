package com.skypro.diploma.repository;

import com.skypro.diploma.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Ad}.
 * Предоставляет стандартные CRUD-операции и кастомные методы поиска.
 */
@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    /**
     * Найти все объявления конкретного автора.
     * Используется для эндпоинта GET /ads/me (объявления текущего пользователя).
     *
     * @param authorId ID автора
     * @return список объявлений автора
     */
    List<Ad> findAllByAuthorId(Long authorId);

    /**
     * Найти все активные объявления, отсортированные по дате создания
     * (сначала новые). Используется для эндпоинта GET /ads.
     *
     * @return список активных объявлений
     */
    List<Ad> findAllByActiveTrueOrderByCreatedAtDesc();
}
