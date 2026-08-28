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
 * Все операции учитывают признак активности (soft-delete):
 * список возвращает только активные комментарии, а изменение или удаление
 * мягко удалённого комментария невозможно — вернётся 404
 * (замечание наставника о повторном изменении удалённого комментария).
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
     * Возвращает все АКТИВНЫЕ комментарии к объявлению (старые сверху).
     * GET /ads/{adId}/comments.
     *
     * @param adId ID объявления
     * @return список DTO комментариев
     * @throws NotFoundException если объявление не найдено или удалено (→ 404)
     */
    public CommentsDto getCommentsByAdId(Long adId) {
        Ad ad = findActiveAd(adId);

        List<Comment> comments =
                commentRepository.findAllByAdIdAndActiveTrueOrderByCreatedAtAsc(ad.getId());
        List<CommentDto> commentDtos = commentMapper.toDtoList(comments);

        CommentsDto result = new CommentsDto();
        result.setCount(commentDtos.size());
        result.setResults(commentDtos);
        return result;
    }

    /**
     * Добавляет комментарий к активному объявлению.
     * POST /ads/{adId}/comments. Автор — текущий пользователь.
     *
     * @param adId           ID объявления
     * @param createCommentReq текст комментария
     * @return DTO созданного комментария
     * @throws NotFoundException если объявление не найдено или удалено (→ 404)
     */
    @Transactional
    public CommentDto addComment(Long adId, CreateCommentReq createCommentReq) {
        Ad ad = findActiveAd(adId);
        User currentUser = userService.getCurrentUser();

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
     * Обновляет текст комментария. PATCH /ads/{adId}/comments/{commentId}.
     * Доступно только автору комментария или администратору.
     *
     * 🆕 Мягко удалённый комментарий обновить нельзя — вернётся 404.
     *
     * @param adId             ID объявления (проверка принадлежности)
     * @param commentId        ID комментария
     * @param createCommentReq новый текст
     * @return обновлённое DTO комментария
     * @throws NotFoundException  если комментарий/объявление не найдены или удалены (→ 404)
     * @throws ForbiddenException если нет прав (→ 403)
     */
    @Transactional
    public CommentDto updateComment(Long adId, Long commentId, CreateCommentReq createCommentReq) {
        Comment comment = findCommentInAd(adId, commentId);

        checkPermission(comment);

        commentMapper.updateCommentFromDto(createCommentReq, comment);
        comment.setUpdatedAt(LocalDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Обновлен комментарий: id={}", commentId);

        return commentMapper.toDto(comment);
    }

    /**
     * Мягко удаляет комментарий. DELETE /ads/{adId}/comments/{commentId}.
     * Доступно только автору комментария или администратору.
     *
     * 🆕 Повторное удаление уже удалённого комментария вернёт 404.
     *
     * @param adId      ID объявления (проверка принадлежности)
     * @param commentId ID комментария
     * @throws NotFoundException  если комментарий/объявление не найдены или удалены (→ 404)
     * @throws ForbiddenException если нет прав (→ 403)
     */
    @Transactional
    public void deleteComment(Long adId, Long commentId) {
        Comment comment = findCommentInAd(adId, commentId);

        checkPermission(comment);

        comment.setActive(false);
        commentRepository.save(comment);
        log.info("Удален комментарий (мягкое удаление): id={}", commentId);
    }

    /**
     * Находит АКТИВНОЕ объявление по ID — единая точка проверки активности.
     *
     * @param adId ID объявления
     * @return сущность объявления
     * @throws NotFoundException если объявления нет или оно мягко удалено
     */
    private Ad findActiveAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + adId + " не найдено"));

        if (!ad.isActive()) {
            throw new NotFoundException("Объявление с id=" + adId + " удалено");
        }
        return ad;
    }

    /**
     * Находит комментарий и проверяет, что он принадлежит указанному объявлению.
     *
     * 🆕 Также проверяет признак активности комментария: работа с мягко
     * удалённым комментарием невозможна — вернётся 404.
     *
     * @param adId      ID объявления из URL
     * @param commentId ID комментария
     * @return сущность комментария
     * @throws NotFoundException если комментарий не найден, принадлежит другому
     *                           объявлению или мягко удалён
     */
    private Comment findCommentInAd(Long adId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        if (!comment.getAd().getId().equals(adId)) {
            throw new NotFoundException(
                    "Комментарий с id=" + commentId + " не принадлежит объявлению " + adId);
        }

        // 🆕 Мягко удалённый комментарий изменить/удалить повторно нельзя
        if (!comment.isActive()) {
            throw new NotFoundException("Комментарий с id=" + commentId + " удален");
        }

        if (!comment.getAd().isActive()) {
            throw new NotFoundException("Объявление с id=" + adId + " удалено");
        }

        return comment;
    }

    /**
     * Проверяет права на комментарий: текущий пользователь — автор или ADMIN.
     *
     * @param comment комментарий
     * @throws ForbiddenException если прав нет
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
