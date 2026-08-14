package com.skypro.diploma.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Комментарий к объявлению")
public class CommentDto {

    @Schema(description = "ID комментария",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "ID автора комментария",
            example = "1")
    private Long author;

    @Schema(description = "Имя автора комментария",
            example = "John Doe")
    private String authorName;

    @Schema(description = "Текст комментария",
            example = "Отличное предложение!")
    private String text;

    @Schema(description = "Дата создания",
            example = "2026-08-12 10:30:00")
    private String createdAt;
}
