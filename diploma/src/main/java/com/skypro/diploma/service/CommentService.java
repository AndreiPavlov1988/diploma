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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

    public CommentsDto getCommentsByAdId(Long adId) {
        Ad ad = findActiveAd(adId);

        // 🆕 Только активные комментарии
        List<Comment> comments =
                commentRepository.findAllByAdIdAndActiveTrueOrderByCreatedAtAsc(ad.getId());
        List<CommentDto> commentDtos = commentMapper.toDtoList(comments);

        CommentsDto result = new CommentsDto();
        result.setCount(commentDtos.size());
        result.setResults(commentDtos);

        return result;
    }

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

    @Transactional
    public void deleteComment(Long adId, Long commentId) {
        Comment comment = findCommentInAd(adId, commentId);
        checkPermission(comment);

        comment.setActive(false);
        commentRepository.save(comment);
        log.info("Удален комментарий (мягкое удаление): id={}", commentId);
    }

    private Ad findActiveAd(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + adId + " не найдено"));

        if (!ad.isActive()) {
            throw new NotFoundException("Объявление с id=" + adId + " удалено");
        }
        return ad;
    }

    private Comment findCommentInAd(Long adId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id=" + commentId + " не найден"));

        if (!comment.getAd().getId().equals(adId)) {
            throw new NotFoundException(
                    "Комментарий с id=" + commentId + " не принадлежит объявлению " + adId);
        }

        if (!comment.getAd().isActive()) {
            throw new NotFoundException("Объявление с id=" + adId + " удалено");
        }

        return comment;
    }

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
