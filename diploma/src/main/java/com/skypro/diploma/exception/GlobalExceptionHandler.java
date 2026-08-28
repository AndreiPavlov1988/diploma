package com.skypro.diploma.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Глобальный обработчик исключений.
 * Ловит исключения из контроллеров и превращает их в правильные HTTP-коды.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 404 — сущность не найдена или мягко удалена */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    /** 403 — нет прав доступа (автор или админ) */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", ex.getMessage()));
    }

    /** 409 — конфликт данных (email занят) */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    /** 400 — неверный текущий пароль при смене пароля */
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        log.warn("400 Bad Request (invalid password): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    /** 400 — недопустимый файл изображения (валидация типа) */
    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<Map<String, String>> handleInvalidImage(InvalidImageException ex) {
        log.warn("400 Bad Request (invalid image): {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    /** 401 — неверные учетные данные при входе */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleUnauthorized(RuntimeException ex) {
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Неверные учетные данные"));
    }

    /** 400 — ошибка валидации (@Valid на DTO) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("400 Bad Request: ошибка валидации");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Неверные данные в запросе"));
    }

    /** 500 — ВНУТРЕННЯЯ ошибка сервера (непредвиденные исключения) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Внутренняя ошибка сервера"));
    }
}