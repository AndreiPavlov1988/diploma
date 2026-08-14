package com.skypro.diploma.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Изображения", description = "API для получения изображений")
public class ImageController {

    @Operation(summary = "Получить изображение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изображение найдено",
                    content = @Content(mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Изображение не найдено")
    })
    @GetMapping(
            value = "/images/{type}/{id}/{filename}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE}
    )
    public ResponseEntity<byte[]> getImage(
            @PathVariable String type,
            @PathVariable Long id,
            @PathVariable String filename) {
        // TODO: Этап IV - реализовать получение изображения
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping(
            value = "/images/{imageId}",
            produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE}
    )
    public ResponseEntity<byte[]> getImageById(@PathVariable Long imageId) {
        // TODO: Этап IV - реализовать получение изображения по ID
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
