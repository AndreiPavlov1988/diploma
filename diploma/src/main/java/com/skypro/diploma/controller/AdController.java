package com.skypro.diploma.controller;

import com.skypro.diploma.dto.ad.AdDto;
import com.skypro.diploma.dto.ad.AdsDto;
import com.skypro.diploma.dto.ad.CreateAdReq;
import com.skypro.diploma.dto.ad.FullAdDto;
import com.skypro.diploma.dto.ad.UpdateAdReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/ads")
@RequiredArgsConstructor
@Tag(name = "Объявления", description = "API для управления объявлениями")
public class AdController {

    @Operation(summary = "Получить все объявления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список объявлений получен успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdsDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping
    public ResponseEntity<AdsDto> getAllAds() {
        // TODO: Этап III - получить все объявления из БД
        AdsDto adsDto = new AdsDto();
        adsDto.setCount(0);
        adsDto.setResults(new ArrayList<>());
        return ResponseEntity.ok(adsDto);
    }

    @Operation(summary = "Создать новое объявление")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Объявление создано успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdDto> createAd(
            @Valid @RequestPart("properties") CreateAdReq createAdReq,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        // TODO: Этап III и IV - сохранить объявление и изображение
        AdDto adDto = createTestAdDto(createAdReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(adDto);
    }

    @Operation(summary = "Получить объявление по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление найдено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = FullAdDto.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FullAdDto> getAd(@PathVariable Long id) {
        // TODO: Этап III - получить объявление из БД
        return ResponseEntity.ok(createTestFullAdDto(id));
    }

    @Operation(summary = "Удалить объявление")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Объявление удалено успешно"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на удаление"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        // TODO: Этап III - удалить объявление
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить объявление")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Объявление обновлено успешно",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AdDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные в запросе"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на редактирование"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<AdDto> updateAd(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdReq updateReq) {
        // TODO: Этап III - обновить объявление
        AdDto adDto = createTestAdDto(updateReq, id);
        return ResponseEntity.ok(adDto);
    }

    @Operation(summary = "Обновить изображение объявления")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изображение обновлено успешно"),
            @ApiResponse(responseCode = "400", description = "Неверный формат файла"),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "403", description = "Нет прав на изменение"),
            @ApiResponse(responseCode = "404", description = "Объявление не найдено")
    })
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateAdImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        // TODO: Этап IV - сохранить изображение
        return ResponseEntity.ok("/images/ads/" + id + "/photo.jpg");
    }

    private AdDto createTestAdDto(CreateAdReq req) {
        AdDto dto = new AdDto();
        dto.setId(1L);
        dto.setTitle(req.getTitle());
        dto.setPrice(req.getPrice());
        dto.setDescription(req.getDescription());
        dto.setAuthor(1L);
        dto.setImage("/images/ads/1/photo.jpg");
        dto.setCreatedAt("2026-08-13 10:30:00");
        return dto;
    }

    private AdDto createTestAdDto(UpdateAdReq req, Long id) {
        AdDto dto = new AdDto();
        dto.setId(id);
        dto.setTitle(req.getTitle());
        dto.setPrice(req.getPrice());
        dto.setDescription(req.getDescription());
        dto.setAuthor(1L);
        dto.setImage("/images/ads/" + id + "/photo.jpg");
        dto.setCreatedAt("2026-08-13 10:30:00");
        return dto;
    }

    private FullAdDto createTestFullAdDto(Long id) {
        FullAdDto dto = new FullAdDto();
        dto.setId(id);
        dto.setTitle("iPhone 15 Pro");
        dto.setPrice(99900);
        dto.setDescription("Отличный смартфон в идеальном состоянии");
        dto.setAuthor(1L);
        dto.setAuthorName("John Doe");
        dto.setImage("/images/ads/" + id + "/photo.jpg");
        dto.setCreatedAt("2026-08-13 10:30:00");
        return dto;
    }
}
