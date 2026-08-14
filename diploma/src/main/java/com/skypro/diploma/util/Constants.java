package com.skypro.diploma.util;

/**
 * Класс для хранения констант проекта.
 * Все хардкоды выносятся сюда.
 */
public final class Constants {

    private Constants() {
        // Приватный конструктор, чтобы нельзя было создать экземпляр
    }

    // ===== API URL =====
    public static final String API_PREFIX = "/api";
    public static final String AUTH_URL = "/auth";
    public static final String USERS_URL = "/users";
    public static final String ADS_URL = "/ads";
    public static final String COMMENTS_URL = "/comments";
    public static final String IMAGES_URL = "/images";

    // ===== Роли =====
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // ===== Параметры для пагинации =====
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String SORT_BY_DEFAULT = "createdAt";
    public static final String SORT_DIRECTION_DEFAULT = "DESC";

    // ===== Настройки файлов =====
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    public static final String ALLOWED_IMAGE_TYPES = "image/jpeg,image/png,image/gif,image/webp";
    public static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};

    // ===== Пути к изображениям =====
    public static final String USERS_IMAGES_PATH = "/images/users/";
    public static final String ADS_IMAGES_PATH = "/images/ads/";

    // ===== Заголовки =====
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String BASIC_PREFIX = "Basic ";

    // ===== Сообщения об ошибках =====
    public static final String ERROR_USER_NOT_FOUND = "Пользователь не найден";
    public static final String ERROR_AD_NOT_FOUND = "Объявление не найдено";
    public static final String ERROR_COMMENT_NOT_FOUND = "Комментарий не найден";
    public static final String ERROR_IMAGE_NOT_FOUND = "Изображение не найдено";
    public static final String ERROR_ACCESS_DENIED = "Доступ запрещен";
    public static final String ERROR_INVALID_CREDENTIALS = "Неверные учетные данные";
    public static final String ERROR_USER_ALREADY_EXISTS = "Пользователь уже существует";
    public static final String ERROR_INVALID_FILE_TYPE = "Неподдерживаемый тип файла";
    public static final String ERROR_FILE_TOO_LARGE = "Файл слишком большой";
    public static final String ERROR_INVALID_PASSWORD = "Неверный текущий пароль";
}
