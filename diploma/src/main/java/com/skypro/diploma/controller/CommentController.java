package com.skypro.diploma.controller;

import com.skypro.diploma.dto.comment.CommentDto;
import com.skypro.diploma.dto.comment.CommentsDto;
import com.skypro.diploma.dto.comment.CreateCommentReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/ads/{adId}/comments")
@RequiredArgsConstructor
@Tag(name = "Комментарии", description = "API для управления комментариями")
public class CommentController {

    @Operation(summary = "Получить все комментарии к объявлению")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список комментариев получен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentsDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @GetMapping
    public ResponseEntity<CommentsDto> getComments(@PathVariable Long adId) {
        // TODO: Этап III - получить комментарии из БД
        CommentsDto dto = new CommentsDto();
        dto.setCount(0);
        dto.setResults(new ArrayList<>());
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Добавить комментарий к объявлению")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий добавлен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PostMapping
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long adId,
            @Valid @RequestBody CreateCommentReq commentReq) {
        // TODO: Этап III - сохранить комментарий
        CommentDto dto = createTestCommentDto(commentReq);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Удалить комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Комментарий удален успешно"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление"),
            @ApiResponse(responseCode = "404", description = "Комментарий не найден")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long adId,
            @PathVariable Long commentId) {
        // TODO: Этап III - удалить комментарий
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить комментарий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Комментарий обновлен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CommentDto.class))),
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
        // TODO: Этап III - обновить комментарий
        CommentDto dto = createTestCommentDto(commentReq);
        dto.setId(commentId);
        return ResponseEntity.ok(dto);
    }

    private CommentDto createTestCommentDto(CreateCommentReq req) {
        CommentDto dto = new CommentDto();
        dto.setId(1L);
        dto.setAuthor(1L);
        dto.setAuthorName("John Doe");
        dto.setText(req.getText());
        dto.setCreatedAt("2026-08-13 10:30:00");
        return dto;
    }
}
