package com.skypro.diploma.dto.ad;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Запрос на создание объявления")
public class CreateAdReq {

    @NotBlank(message = "Заголовок не может быть пустым")
    @Schema(description = "Заголовок объявления",
            example = "iPhone 15 Pro",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "Цена не может быть пустой")
    @Positive(message = "Цена должна быть положительной")
    @Schema(description = "Цена в рублях",
            example = "99900",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer price;

    @NotBlank(message = "Описание не может быть пустым")
    @Schema(description = "Описание объявления",
            example = "Отличный смартфон в идеальном состоянии",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "Изображение (будет загружено отдельным запросом)",
            hidden = true)
    private String image;
}
