package com.skypro.diploma.dto.ad;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Список объявлений")
public class AdsDto {

    @Schema(description = "Общее количество объявлений",
            example = "10")
    private Integer count;

    @Schema(description = "Список объявлений")
    private List<AdDto> results = new ArrayList<>();
}
