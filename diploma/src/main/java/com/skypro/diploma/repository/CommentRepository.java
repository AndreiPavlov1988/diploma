package com.skypro.diploma.repository;

import com.skypro.diploma.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Comment}.
 * Предоставляет стандартные CRUD-операции и кастомные методы поиска.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Найти все комментарии к объявлению, отсортированные по дате создания
     * (сначала старые). Используется для эндпоинта GET /ads/{adId}/comments.
     *
     * @param adId ID объявления
     * @return список комментариев к объявлению
     */
    List<Comment> findAllByAdIdOrderByCreatedAtAsc(Long adId);

    /**
     * Найти все комментарии конкретного автора.
     * Может пригодиться для проверки прав при удалении/редактировании.
     *
     * @param authorId ID автора
     * @return список комментариев автора
     */
    List<Comment> findAllByAuthorId(Long authorId);

    /**
     * Проверить, существуют ли комментарии у объявления.
     *
     * @param adId ID объявления
     * @return true, если у объявления есть комментарии
     */
    boolean existsByAdId(Long adId);
}
