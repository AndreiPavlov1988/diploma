package com.skypro.diploma.controller;

import com.skypro.diploma.dto.comment.CommentDto;
import com.skypro.diploma.dto.comment.CommentsDto;
import com.skypro.diploma.dto.comment.CreateCommentReq;
import com.skypro.diploma.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Контроллер комментариев к объявлениям.
 * URL начинается с /ads/{adId}/comments — это вложенный ресурс.
 */
@RestController
@RequestMapping("/ads/{adId}/comments")
@RequiredArgsConstructor
@Tag(name = "Комментарии", description = "API для управления комментариями")
public class CommentController {

    private final CommentService commentService;

    /**
     * Получить все комментарии к объявлению (GET /ads/{adId}/comments).
     */
    @Operation(summary = "Получение комментариев объявления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев получен"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @GetMapping
    public ResponseEntity<CommentsDto> getComments(@PathVariable Long adId) {
        return ResponseEntity.ok(commentService.getCommentsByAdId(adId));
    }

    /**
     * Добавить комментарий (POST /ads/{adId}/comments).
     */
    @Operation(summary = "Добавление комментария к объявлению")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий добавлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PostMapping
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long adId,
            @Valid @RequestBody CreateCommentReq commentReq) {
        return ResponseEntity.ok(commentService.addComment(adId, commentReq));
    }

    /**
     * Обновить комментарий (PATCH /ads/{adId}/comments/{commentId}).
     * Только автор или администратор (проверяется в сервисе).
     */
    @Operation(summary = "Обновление комментария")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на редактирование"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long adId,
            @PathVariable Long commentId,
            @Valid @RequestBody CreateCommentReq commentReq) {
        return ResponseEntity.ok(commentService.updateComment(adId, commentId, commentReq));
    }

    /**
     * Удалить комментарий (DELETE /ads/{adId}/comments/{commentId}).
     * Возвращает код 204 No Content.
     */
    @Operation(summary = "Удаление комментария")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий удален"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long adId,
            @PathVariable Long commentId) {
        commentService.deleteComment(adId, commentId);
        return ResponseEntity.noContent().build();
    }
}
