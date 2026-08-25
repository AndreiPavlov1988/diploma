package com.skypro.diploma.repository;

import com.skypro.diploma.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностью Comment.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Найти все АКТИВНЫЕ комментарии к объявлению (старые сверху).
     * Мягко удалённые (is_active = false) НЕ возвращаются.
     */
    List<Comment> findAllByAdIdAndActiveTrueOrderByCreatedAtAsc(Long adId);

    /**
     * Найти все комментарии конкретного автора.
     */
    List<Comment> findAllByAuthorId(Long authorId);

    /**
     * Проверить, существуют ли комментарии у объявления.
     */
    boolean existsByAdId(Long adId);
}
