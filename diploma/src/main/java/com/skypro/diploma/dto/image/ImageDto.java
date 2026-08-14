package com.skypro.diploma.dto.image;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об изображении")
public class ImageDto {

    @Schema(description = "ID изображения",
            example = "1")
    private Long id;

    @Schema(description = "URL изображения",
            example = "/images/ads/1/photo.jpg")
    private String url;

    @Schema(description = "Имя файла",
            example = "photo.jpg")
    private String fileName;

    @Schema(description = "Размер файла в байтах",
            example = "1024000")
    private Long fileSize;

    @Schema(description = "Тип файла",
            example = "image/jpeg")
    private String contentType;
}
