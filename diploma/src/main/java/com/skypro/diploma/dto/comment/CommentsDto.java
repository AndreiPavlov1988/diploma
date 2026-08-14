package com.skypro.diploma.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Список комментариев")
public class CommentsDto {

    @Schema(description = "Общее количество комментариев",
            example = "5")
    private Integer count;

    @Schema(description = "Список комментариев")
    private List<CommentDto> results = new ArrayList<>();
}
