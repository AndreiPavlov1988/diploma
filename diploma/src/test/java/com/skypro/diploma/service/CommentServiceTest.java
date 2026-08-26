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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты CommentService.
 * Главное: фильтрация удалённых комментариев и защита от работы с удалёнными объявлениями.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AdRepository adRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private UserService userService;

    @InjectMocks
    private CommentService commentService;

    private User user(Long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setUsername("user" + id + "@mail.ru");
        return u;
    }

    private Ad ad(Long id, User author, boolean active) {
        Ad a = new Ad();
        a.setId(id);
        a.setAuthor(author);
        a.setActive(active);
        return a;
    }

    private Comment comment(Long id, User author, Ad ad, boolean active) {
        Comment c = new Comment();
        c.setId(id);
        c.setAuthor(author);
        c.setAd(ad);
        c.setActive(active);
        c.setText("Комментарий " + id);
        return c;
    }

    @Test
    void addComment_toInactiveAd_throwsNotFound() {
        when(adRepository.findById(1L))
                .thenReturn(Optional.of(ad(1L, user(1L, Role.USER), false))); // удалённое объявление

        // нельзя комментировать удалённое объявление -> 404
        assertThatThrownBy(() -> commentService.addComment(1L, new CreateCommentReq()))
                .isInstanceOf(NotFoundException.class);

        verify(commentRepository, never()).save(any());
    }

    @Test
    void getCommentsByAdId_returnsOnlyActiveComments() {
        Ad ad = ad(1L, user(1L, Role.USER), true);
        Comment active = comment(10L, user(2L, Role.USER), ad, true);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(commentRepository.findAllByAdIdAndActiveTrueOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(active));
        when(commentMapper.toDtoList(List.of(active))).thenReturn(List.of(new CommentDto()));

        CommentsDto result = commentService.getCommentsByAdId(1L);

        // удалённые комментарии не показываются
        assertThat(result.getCount()).isEqualTo(1);
    }

    @Test
    void deleteComment_byNonAuthor_throwsForbidden() {
        Ad ad = ad(1L, user(1L, Role.USER), true);
        Comment comment = comment(10L, user(2L, Role.USER), ad, true);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userService.getCurrentUser()).thenReturn(user(3L, Role.USER)); // чужой

        // чужой комментарий удалить нельзя -> 403
        assertThatThrownBy(() -> commentService.deleteComment(1L, 10L))
                .isInstanceOf(ForbiddenException.class);

        assertThat(comment.isActive()).isTrue();
    }

    @Test
    void updateComment_commentFromAnotherAd_throwsNotFound() {
        Ad ad = ad(1L, user(1L, Role.USER), true);
        Comment comment = comment(10L, user(1L, Role.USER), ad, true);
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        // комментарий 10 принадлежит объявлению 1, а не 999 -> 404
        assertThatThrownBy(() -> commentService.updateComment(999L, 10L, new CreateCommentReq()))
                .isInstanceOf(NotFoundException.class);
    }
}
