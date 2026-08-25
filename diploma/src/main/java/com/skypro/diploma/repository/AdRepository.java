package com.skypro.diploma.repository;

import com.skypro.diploma.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Ad}.
 */
@Repository
public interface AdRepository extends JpaRepository<Ad, Long> {

    /**
     * Найти все активные объявления (новые сверху).
     * Используется для GET /ads.
     *
     * @return список активных объявлений
     */
    List<Ad> findAllByActiveTrueOrderByCreatedAtDesc();

    /**
     * 🆕 Найти все АКТИВНЫЕ объявления автора.
     * Мягко удалённые (is_active = false) НЕ возвращаются —
     * исправляет замечание наставника про /ads/me.
     *
     * @param authorId ID автора
     * @return список активных объявлений автора
     */
    List<Ad> findAllByAuthorIdAndActiveTrue(Long authorId);
}
