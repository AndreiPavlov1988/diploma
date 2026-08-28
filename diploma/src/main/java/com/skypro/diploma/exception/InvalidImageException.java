package com.skypro.diploma.exception;

/**
 * Исключение: загруженный файл не является допустимым изображением
 * (неверное расширение или content-type).
 *
 * Глобальный обработчик превращает в HTTP 400 Bad Request —
 * это ошибка клиента, а не сервера.
 */
public class InvalidImageException extends RuntimeException {

    /**
     * @param message описание причины отклонения файла
     */
    public InvalidImageException(String message) {
        super(message);
    }
}