package com.skypro.diploma.exception;

/**
 * Исключение для случаев конфликта данных.
 * Например: при регистрации email уже занят другим пользователем.
 *
 * Глобальный обработчик превращает это исключение в HTTP-ответ 409 Conflict.
 *
 * Согласно спецификации OpenAPI, ответ 409 должен возвращаться при попытке
 * создать пользователя с уже существующим email.
 */
public class ConflictException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текст ошибки (например, "Пользователь с таким email уже существует")
     */
    public ConflictException(String message) {
        super(message);
    }
}
