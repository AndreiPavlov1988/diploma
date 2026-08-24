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
 *
 * Аннотация @RestControllerAdvice говорит Spring:
 * "Следи за ВСЕМИ контроллерами приложения. Если где-то возникнет исключение —
 * перехвати его здесь и верни правильный HTTP-ответ".
 *
 * Это избавляет нас от необходимости писать try-catch в каждом контроллере.
 */
@Slf4j  // Lombok автоматически создает поле log для логирования
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработчик NotFoundException.
     * Возвращает HTTP 404 Not Found.
     *
     * @param ex перехваченное исключение
     * @return ответ с кодом 404 и JSON {"message": "..."}
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());  // логируем в консоль
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  // код 404
                .body(Map.of("message", ex.getMessage()));  // тело ответа
    }

    /**
     * Обработчик ForbiddenException.
     * Возвращает HTTP 403 Forbidden (нет прав доступа).
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenException ex) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)  // код 403
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Обработчик ConflictException.
     * Возвращает HTTP 409 Conflict (например, email уже занят).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)  // код 409
                .body(Map.of("message", ex.getMessage()));
    }

    /**
     * Обработчик ошибок аутентификации Spring Security.
     * Возвращает HTTP 401 Unauthorized.
     *
     * Срабатывает при неверном логине/пароле.
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleUnauthorized(RuntimeException ex) {
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)  // код 401
                .body(Map.of("message", "Неверные учетные данные"));
    }

    /**
     * Обработчик ошибок валидации (@Valid, @NotBlank и т.д.).
     * Возвращает HTTP 400 Bad Request.
     *
     * Срабатывает, когда в запросе некорректные данные:
     * пустое поле, неверный формат email, слишком короткий пароль.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        log.warn("400 Bad Request: ошибка валидации - {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // код 400
                .body(Map.of("message", "Неверные данные в запросе"));
    }

    /**
     * Обработчик всех остальных RuntimeException.
     * Возвращает HTTP 400 Bad Request.
     *
     * Это "запасной" обработчик — если исключение не поймано выше,
     * оно попадет сюда, и приложение не упадет с 500.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        log.error("Необработанная ошибка: {}", ex.getMessage(), ex);  // логируем со стеком
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)  // код 400
                .body(Map.of("message", ex.getMessage()));
    }
}
