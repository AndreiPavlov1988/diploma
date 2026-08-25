package com.skypro.diploma.exception;

/**
 * Исключение для случаев, когда запрашиваемая сущность не найдена в БД.
 * Например: пользователь с таким ID не существует, объявление удалено и т.д.
 *
 * Глобальный обработчик превращает это исключение в HTTP-ответ 404 Not Found.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текст ошибки (например, "Объявление не найдено")
     */
    public NotFoundException(String message) {
        super(message);  // передаем сообщение в родительский класс RuntimeException
    }
}
