package com.skypro.diploma.exception;

/**
 * Исключение: неверный текущий пароль при смене пароля.
 *
 * 🆕 Выделяется в отдельный тип, чтобы глобальный обработчик возвращал
 * HTTP 400 Bad Request (ошибка клиента), а не 500 (ошибка сервера).
 *
 * Это ожидаемая ошибка валидации — пользователь просто неверно ввёл
 * текущий пароль, это не баг в приложении.
 */
public class InvalidPasswordException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке.
     *
     * @param message текст ошибки (например, "Текущий пароль неверен")
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}
