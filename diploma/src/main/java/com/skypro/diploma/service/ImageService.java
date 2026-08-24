package com.skypro.diploma.service;

import com.skypro.diploma.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Сервис для работы с изображениями.
 *
 * Хранит картинки НА ДИСКЕ в папке images/ (предпочтительный способ по ТЗ),
 * а в базу данных сохраняется только путь к файлу (поле image_path).
 *
 * Структура папок на диске:
 * images/
 * ├── ads/        ← картинки объявлений
 * │   └── 1/      ← папка объявления с ID=1
 * │       └── abc-123.jpg
 * └── users/      ← аватарки пользователей
 *     └── 2/
 *         └── def-456.png
 */
@Slf4j
@Service
public class ImageService {

    /**
     * Путь к папке хранения картинок.
     * Берется из application.properties: app.images.storage-path=./images
     * Если свойства нет — используется значение по умолчанию ./images
     */
    @Value("${app.images.storage-path:./images}")
    private String storagePath;

    /**
     * Сохраняет загруженную картинку на диск.
     *
     * @param image    файл, загруженный пользователем (MultipartFile)
     * @param type     тип сущности: "ads" или "users"
     * @param entityId ID сущности (ID объявления или пользователя)
     * @return URL-путь для БД и фронтенда, например "/images/ads/1/abc.jpg"
     * @throws IOException если не удалось записать файл на диск
     */
    public String saveImage(MultipartFile image, String type, Long entityId) throws IOException {
        // Если файл не загрузили — возвращаем null (картинка не обязательна)
        if (image == null || image.isEmpty()) {
            return null;
        }

        // 1. Создаем папку images/{type}/{id}/, если её еще нет
        //    Например: images/ads/1/
        Path uploadPath = Paths.get(storagePath, type, String.valueOf(entityId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. Генерируем УНИКАЛЬНОЕ имя файла, чтобы файлы не перезаписывались
        //    UUID.randomUUID() → например "9c0f3a2e-9b6d-4c6f-8a1e-2f3b4c5d6e7f"
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            // сохраняем расширение: .jpg, .png, .gif
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + extension;

        // 3. Записываем байты файла на диск
        Path filePath = uploadPath.resolve(filename);
        Files.copy(image.getInputStream(), filePath);

        log.info("Сохранено изображение: {}", filePath.toAbsolutePath());

        // 4. Возвращаем относительный URL — его фронтенд подставит к http://localhost:8080
        return String.format("/images/%s/%d/%s", type, entityId, filename);
    }

    /**
     * Читает байты картинки с диска (для отдачи фронтенду).
     *
     * @param path путь из БД, например "/images/ads/1/abc.jpg"
     * @return байты файла
     * @throws NotFoundException если файла нет на диске (→ HTTP 404)
     */
    public byte[] getImage(String path) {
        // Убираем начальный слеш: "/images/..." → "images/..."
        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        Path filePath = Paths.get(relativePath);

        if (!Files.exists(filePath)) {
            throw new NotFoundException("Изображение не найдено: " + path);
        }

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Ошибка чтения изображения: {}", path, e);
            throw new NotFoundException("Ошибка чтения изображения: " + path);
        }
    }
}
