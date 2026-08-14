package com.skypro.diploma.dto.ad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Краткая информация об объявлении")
public class AdDto {

    @Schema(description = "ID объявления",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Заголовок объявления",
            example = "iPhone 15 Pro")
    private String title;

    @Schema(description = "Цена в рублях",
            example = "99900")
    private Integer price;

    @Schema(description = "Описание объявления",
            example = "Отличный смартфон в идеальном состоянии")
    private String description;

    @Schema(description = "ID автора объявления",
            example = "1")
    private Long author;

    @Schema(description = "URL изображения",
            example = "/images/ads/1/photo.jpg")
    private String image;

    @Schema(description = "Дата создания",
            example = "2026-08-12 10:30:00")
    private String createdAt;
}
