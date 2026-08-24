package com.skypro.diploma.service;

import com.skypro.diploma.dto.ad.AdDto;
import com.skypro.diploma.dto.ad.AdsDto;
import com.skypro.diploma.dto.ad.CreateAdReq;
import com.skypro.diploma.dto.ad.FullAdDto;
import com.skypro.diploma.dto.ad.UpdateAdReq;
import com.skypro.diploma.entity.Ad;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ForbiddenException;
import com.skypro.diploma.exception.NotFoundException;
import com.skypro.diploma.mapper.AdMapper;
import com.skypro.diploma.repository.AdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для работы с объявлениями.
 *
 * Содержит:
 * - CRUD операции (создать, получить, обновить, удалить)
 * - Проверку прав доступа (автор или администратор)
 * - Работу с изображениями через ImageService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;
    private final AdMapper adMapper;
    private final UserService userService;
    private final ImageService imageService;

    /**
     * Получить ВСЕ объявления (только активные, отсортированные по дате — новые сверху).
     * Используется для эндпоинта GET /ads.
     */
    public AdsDto getAllAds() {
        List<Ad> ads = adRepository.findAllByActiveTrueOrderByCreatedAtDesc();
        List<AdDto> adDtos = adMapper.toDtoList(ads);

        AdsDto result = new AdsDto();
        result.setCount(adDtos.size());
        result.setResults(adDtos);

        return result;
    }

    /**
     * Получить объявления ТЕКУЩЕГО пользователя (только активные).
     * Используется для эндпоинта GET /ads/me.
     */
    public AdsDto getMyAds() {
        User currentUser = userService.getCurrentUser();
        List<Ad> ads = adRepository.findAllByAuthorId(currentUser.getId());
        List<AdDto> adDtos = adMapper.toDtoList(ads);

        AdsDto result = new AdsDto();
        result.setCount(adDtos.size());
        result.setResults(adDtos);

        return result;
    }

    /**
     * Получить объявление по ID с ПОЛНОЙ информацией (включая имя автора).
     * Используется для эндпоинта GET /ads/{id}.
     *
     * @param id ID объявления
     * @throws NotFoundException если объявление не найдено или удалено (→ 404)
     */
    public FullAdDto getAdById(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + id + " не найдено"));

        if (!ad.isActive()) {
            throw new NotFoundException("Объявление с id=" + id + " удалено");
        }

        return adMapper.toFullAdDto(ad);
    }

    /**
     * Создать новое объявление (возможно с изображением).
     * Используется для эндпоинта POST /ads.
     *
     * Логика работы:
     * 1. Сначала сохраняем объявление БЕЗ картинки (чтобы получить ID)
     * 2. Если есть картинка — сохраняем её с этим ID
     * 3. Обновляем путь к картинке в объявлении
     *
     * @param createAdReq данные объявления
     * @param image       загруженный файл (может быть null)
     * @return DTO созданного объявления с присвоенным ID
     */
    @Transactional
    public AdDto createAd(CreateAdReq createAdReq, MultipartFile image) throws IOException {
        User currentUser = userService.getCurrentUser();

        // 1. DTO -> Entity
        Ad ad = adMapper.toEntity(createAdReq);
        ad.setAuthor(currentUser);
        ad.setActive(true);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());

        // 2. Сохраняем БЕЗ картинки (нужен ID для пути файла)
        ad = adRepository.save(ad);
        log.info("Создано объявление без картинки: id={}, автор={}",
                ad.getId(), currentUser.getUsername());

        // 3. Если есть картинка — сохраняем её
        if (image != null && !image.isEmpty()) {
            String imagePath = imageService.saveImage(image, "ads", ad.getId());
            ad.setImagePath(imagePath);
            ad = adRepository.save(ad);
            log.info("Добавлена картинка к объявлению: id={}", ad.getId());
        }

        return adMapper.toDto(ad);
    }

    /**
     * Обновить объявление (только автор или администратор).
     * Используется для эндпоинта PATCH /ads/{id}.
     *
     * @param id        ID объявления
     * @param updateReq новые данные
     * @throws NotFoundException  если объявление не найдено (→ 404)
     * @throws ForbiddenException если нет прав (→ 403)
     */
    @Transactional
    public AdDto updateAd(Long id, UpdateAdReq updateReq) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + id + " не найдено"));

        // Проверяем права ДО изменений
        checkPermission(ad);

        // Обновляем поля через MapStruct
        adMapper.updateAdFromDto(updateReq, ad);
        ad.setUpdatedAt(LocalDateTime.now());

        ad = adRepository.save(ad);
        log.info("Обновлено объявление: id={}", id);

        return adMapper.toDto(ad);
    }

    /**
     * Удалить объявление (только автор или администратор).
     * Используется для эндпоинта DELETE /ads/{id}.
     *
     * Используем "мягкое удаление" — не удаляем из БД, а помечаем как неактивное.
     * Это сохраняет целостность данных (комментарии, связи).
     */
    @Transactional
    public void deleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + id + " не найдено"));

        checkPermission(ad);

        ad.setActive(false);
        adRepository.save(ad);
        log.info("Удалено объявление (мягкое удаление): id={}", id);
    }

    /**
     * Обновить изображение объявления (только автор или администратор).
     * Используется для эндпоинта PATCH /ads/{id}/image.
     *
     * @param id    ID объявления
     * @param image новый файл изображения
     * @return URL новой картинки
     */
    @Transactional
    public String updateAdImage(Long id, MultipartFile image) throws IOException {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + id + " не найдено"));

        checkPermission(ad);

        String imagePath = imageService.saveImage(image, "ads", id);
        ad.setImagePath(imagePath);
        ad.setUpdatedAt(LocalDateTime.now());
        adRepository.save(ad);

        log.info("Обновлена картинка объявления: id={}", id);
        return imagePath;
    }

    /**
     * Проверка прав доступа к объявлению.
     *
     * По ТЗ:
     * - Обычный пользователь может редактировать/удалять ТОЛЬКО свои объявления
     * - Администратор может редактировать/удалять ЛЮБЫЕ объявления
     *
     * @param ad объявление, к которому обращается пользователь
     * @throws ForbiddenException если нет прав (→ 403)
     */
    private void checkPermission(Ad ad) {
        User currentUser = userService.getCurrentUser();

        boolean isAuthor = ad.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            throw new ForbiddenException(
                    "Нет прав на выполнение операции с объявлением id=" + ad.getId());
        }
    }
}
