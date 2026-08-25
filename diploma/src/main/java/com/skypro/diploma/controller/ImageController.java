package com.skypro.diploma.controller;

import com.skypro.diploma.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * Контроллер для отдачи изображений.
 *
 * URL: /images/{type}/{id}/{filename}
 * Пример: /images/ads/1/abc-123.jpg
 *
 * Эндпоинт открытый (не требует авторизации) — настроено в SecurityConfig.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Изображения", description = "API для получения изображений")
public class ImageController {

    private final ImageService imageService;

    /**
     * Отдать картинку по её полному пути.
     *
     * @param type     "ads" или "users"
     * @param id       ID объявления или пользователя
     * @param filename имя файла (например, "abc-123.jpg")
     * @return байты файла с правильным Content-Type
     */
    @Operation(summary = "Получить изображение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изображение найдено"),
            @ApiResponse(responseCode = "404", description = "Изображение не найдено")
    })
    @GetMapping(value = "/images/{type}/{id}/{filename}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE})
    public ResponseEntity<byte[]> getImage(
            @PathVariable String type,
            @PathVariable Long id,
            @PathVariable String filename) {

        // 1. Собираем путь: images/ads/1/abc-123.jpg
        String path = String.format("images/%s/%d/%s", type, id, filename);

        // 2. Читаем байты с диска через ImageService
        byte[] image = imageService.getImage(path);

        // 3. Отдаем байты с правильным Content-Type
        return ResponseEntity.ok()
                .contentType(detectMediaType(filename))
                .body(image);
    }

    /**
     * Определяет тип контента (Content-Type) по расширению файла.
     */
    private MediaType detectMediaType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;  // по умолчанию
    }
}
