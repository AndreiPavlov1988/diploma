package com.skypro.diploma.service;

import com.skypro.diploma.dto.comment.CommentDto;
import com.skypro.diploma.dto.comment.CommentsDto;
import com.skypro.diploma.dto.comment.CreateCommentReq;
import com.skypro.diploma.entity.Ad;
import com.skypro.diploma.entity.Comment;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ForbiddenException;
import com.skypro.diploma.exception.NotFoundException;
import com.skypro.diploma.mapper.CommentMapper;
import com.skypro.diploma.repository.AdRepository;
import com.skypro.diploma.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с комментариями к объявлениям.
 *
 * Содержит:
 * - Получение списка комментариев к объявлению
 * - Создание/обновление/удаление комментариев
 * - Проверку прав доступа (автор или администратор)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

    /**
     * Получить все комментарии к объявлению (отсортированы по дате — старые сверху).
     * Используется для эндпоинта GET /ads/{adId}/comments.
     *
     * @param adId ID объявления
     * @throws NotFoundException если объявление не найдено (→ 404)
     */
    public CommentsDto getCommentsByAdId(Long adId) {
        // Проверяем, что объявление существует
        if (!adRepository.existsById(adId)) {
            throw new NotFoundException("Объявление с id=" + adId + " не найдено");
        }

        List<Comment> comments = commentRepository.findAllByAdIdOrderByCreatedAtAsc(adId);
        List<CommentDto> commentDtos = commentMapper.toDtoList(comments);

        CommentsDto result = new CommentsDto();
        result.setCount(commentDtos.size());
        result.setResults(commentDtos);

        return result;
    }

    /**
     * Добавить комментарий к объявлению.
     * Используется для эндпоинта POST /ads/{adId}/comments.
     *
     * @param adId            ID объявления
     * @param createCommentReq текст комментария
     * @return DTO созданного комментария
     * @throws NotFoundException если объявление не найдено (→ 404)
     */
    @Transactional
    public CommentDto addComment(Long adId, CreateCommentReq createCommentReq) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + adId + " не найдено"));

        User currentUser = userService.getCurrentUser();

        // DTO -> Entity
        Comment comment = commentMapper.toEntity(createCommentReq);
        comment.setAd(ad);
        comment.setAuthor(currentUser);
        comment.setActive(true);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Добавлен комментарий к объявлению adId={}, автор={}",
                adId, currentUser.getUsername());

        return commentMapper.toDto(comment);
    }

    /**
     * Обновить комментарий (только автор или администратор).
     * Используется для эндпоинта PATCH /ads/{adId}/comments/{commentId}.
     *
     * @param adId            ID объявления (для проверки принадлежности)
     * @param commentId       ID комментария
     * @param createCommentReq новый текст
     * @throws NotFoundException  если комментарий не найден (→ 404)
     * @throws ForbiddenException если нет прав (→ 403)
     */
    @Transactional
    public CommentDto updateComment(Long adId, Long commentId, CreateCommentReq createCommentReq) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " не найден"));

        // Защита от подмены adId в URL
        if (!comment.getAd().getId().equals(adId)) {
            throw new NotFoundException(
                    "Комментарий с id=" + commentId + " не принадлежит объявлению " + adId);
        }

        // Проверяем права
        checkPermission(comment);

        // Обновляем текст
        commentMapper.updateCommentFromDto(createCommentReq, comment);
        comment.setUpdatedAt(LocalDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Обновлен комментарий: id={}", commentId);

        return commentMapper.toDto(comment);
    }

    /**
     * Удалить комментарий (мягкое удаление, только автор или администратор).
     * Используется для эндпоинта DELETE /ads/{adId}/comments/{commentId}.
     */
    @Transactional
    public void deleteComment(Long adId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(
                        "Комментарий с id=" + commentId + " не найден"));

        if (!comment.getAd().getId().equals(adId)) {
            throw new NotFoundException(
                    "Комментарий с id=" + commentId + " не принадлежит объявлению " + adId);
        }

        checkPermission(comment);

        comment.setActive(false);
        commentRepository.save(comment);
        log.info("Удален комментарий (мягкое удаление): id={}", commentId);
    }

    /**
     * Проверка прав доступа к комментарию.
     *
     * По ТЗ:
     * - Обычный пользователь может редактировать/удалять ТОЛЬКО свои комментарии
     * - Администратор может редактировать/удалять ЛЮБЫЕ комментарии
     *
     * @param comment комментарий
     * @throws ForbiddenException если нет прав (→ 403)
     */
    private void checkPermission(Comment comment) {
        User currentUser = userService.getCurrentUser();

        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException(
                    "Нет прав на выполнение операции с комментарием id=" + comment.getId());
        }
    }
}
