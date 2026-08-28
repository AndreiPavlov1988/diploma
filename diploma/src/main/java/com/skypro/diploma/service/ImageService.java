package com.skypro.diploma.service;

import com.skypro.diploma.exception.InvalidImageException;
import com.skypro.diploma.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для работы с изображениями.
 *
 * Картинки хранятся НА ДИСКЕ (предпочтительный способ по ТЗ),
 * в БД сохраняется только URL-путь (поле image_path).
 *
 * Корневая папка хранения задаётся свойством app.images.storage-path
 * и используется ОДИНАКОВО при записи и при чтении —
 * исправляет замечание наставника о расхождении путей.
 */
@Slf4j
@Service
public class ImageService {

    /** Разрешённые расширения файлов изображений. */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".jpg", ".jpeg", ".png", ".gif");

    /**
     * Корневая папка хранения картинок.
     * Берётся из application.properties, по умолчанию ./images
     */
    @Value("${app.images.storage-path:./images}")
    private String storagePath;

    /**
     * Сохраняет загруженную картинку на диск с валидацией типа файла.
     *
     * @param image    загруженный файл (может быть null — картинка не обязательна)
     * @param type     тип сущности: "ads" или "users"
     * @param entityId ID сущности (объявления или пользователя)
     * @return URL-путь для БД и фронтенда, например "/images/ads/1/abc.jpg";
     *         null, если файл не был передан
     * @throws IOException             если не удалось записать файл на диск
     * @throws InvalidImageException   если файл не является допустимой картинкой (→ 400)
     */
    public String saveImage(MultipartFile image, String type, Long entityId) throws IOException {
        if (image == null || image.isEmpty()) {
            return null;
        }

        // Валидация типа файла (расширение + content-type)
        validateImage(image);

        // Папка images/{type}/{id}/ с учётом настраиваемого пути
        Path uploadDir = Paths.get(storagePath, type, String.valueOf(entityId));
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Уникальное имя файла (UUID) + исходное расширение
        String filename = UUID.randomUUID() + getExtension(image.getOriginalFilename());

        Path filePath = uploadDir.resolve(filename);
        Files.copy(image.getInputStream(), filePath);
        log.info("Сохранено изображение: {}", filePath.toAbsolutePath());

        return String.format("/images/%s/%d/%s", type, entityId, filename);
    }

    /**
     * Читает байты картинки с диска.
     *
     * Путь из БД ("/images/ads/1/abc.jpg") преобразуется в физический путь
     * с учётом app.images.storage-path — чтение работает при любом значении свойства.
     *
     * @param urlPath URL-путь из БД, например "/images/ads/1/abc.jpg"
     * @return байты файла
     * @throws NotFoundException если файла нет на диске (→ 404)
     */
    public byte[] getImage(String urlPath) {
        Path filePath = resolve(urlPath);

        if (!Files.exists(filePath)) {
            throw new NotFoundException("Изображение не найдено: " + urlPath);
        }
        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Ошибка чтения изображения: {}", urlPath, e);
            throw new NotFoundException("Ошибка чтения изображения: " + urlPath);
        }
    }

    /**
     * Удаляет файл картинки с диска (если он существует).
     * Вызывается при ЗАМЕНЕ изображения, чтобы не копить старые файлы.
     * Ошибки удаления не прерывают основной поток — только пишутся в лог.
     *
     * @param urlPath URL-путь старой картинки из БД (может быть null)
     */
    public void deleteImage(String urlPath) {
        if (urlPath == null || urlPath.isBlank()) {
            return;
        }
        try {
            Path filePath = resolve(urlPath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Удалено старое изображение: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.warn("Не удалось удалить старое изображение: {}", urlPath, e);
        }
    }

    /**
     * Преобразует URL-путь ("/images/ads/1/abc.jpg") в физический путь на диске:
     * отбрасывает префикс "/images/" и подставляет настроенный storagePath.
     *
     * @param urlPath URL-путь из БД
     * @return физический путь к файлу
     */
    private Path resolve(String urlPath) {
        String relative = urlPath.startsWith("/") ? urlPath.substring(1) : urlPath;
        if (relative.startsWith("images/")) {
            relative = relative.substring("images/".length());
        }
        return Paths.get(storagePath).resolve(relative);
    }

    /**
     * Валидирует загруженный файл: допускаются только JPG/PNG/GIF
     * и content-type, начинающийся с "image/".
     *
     * @param image загруженный файл
     * @throws InvalidImageException если файл не является допустимой картинкой
     */
    private void validateImage(MultipartFile image) {
        String extension = getExtension(image.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidImageException(
                    "Недопустимый тип файла: разрешены только JPG, PNG, GIF");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("Недопустимый content-type: " + contentType);
        }
    }

    /**
     * Возвращает расширение файла в нижнем регистре (".jpg", ".png"...).
     *
     * @param filename исходное имя файла
     * @return расширение или пустую строку
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase(Locale.ROOT);
    }
}
