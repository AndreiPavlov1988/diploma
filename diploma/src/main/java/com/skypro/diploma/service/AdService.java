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
import com.skypro.diploma.exception.InvalidImageException;
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
 * Сервис для работы с объявлениями: CRUD, проверка прав,
 * мягкое удаление, работа с изображениями.
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
     * Возвращает все АКТИВНЫЕ объявления (новые сверху). GET /ads — публичный.
     *
     * @return список DTO объявлений
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
     * Возвращает АКТИВНЫЕ объявления текущего пользователя. GET /ads/me.
     *
     * @return список DTO объявлений пользователя
     */
    public AdsDto getMyAds() {
        User currentUser = userService.getCurrentUser();
        List<Ad> ads = adRepository.findAllByAuthorIdAndActiveTrue(currentUser.getId());
        List<AdDto> adDtos = adMapper.toDtoList(ads);

        AdsDto result = new AdsDto();
        result.setCount(adDtos.size());
        result.setResults(adDtos);
        return result;
    }

    /**
     * Возвращает активное объявление по ID с полной информацией. GET /ads/{id}.
     *
     * @param id ID объявления
     * @return полное DTO объявления
     * @throws NotFoundException если объявление не найдено или удалено (→ 404)
     */
    public FullAdDto getAdById(Long id) {
        Ad ad = findActiveAd(id);
        return adMapper.toFullAdDto(ad);
    }

    /**
     * Создаёт объявление (возможно, с изображением). POST /ads.
     *
     * @param createAdReq данные объявления
     * @param image       файл картинки (не обязателен)
     * @return DTO созданного объявления
     * @throws IOException           если не удалось записать файл
     * @throws InvalidImageException если переданный файл не является картинкой (→ 400)
     */
    @Transactional
    public AdDto createAd(CreateAdReq createAdReq, MultipartFile image) throws IOException {
        User currentUser = userService.getCurrentUser();

        Ad ad = adMapper.toEntity(createAdReq);
        ad.setAuthor(currentUser);
        ad.setActive(true);
        ad.setCreatedAt(LocalDateTime.now());
        ad.setUpdatedAt(LocalDateTime.now());

        // Сохраняем без картинки, чтобы получить ID
        ad = adRepository.save(ad);

        // Если картинка передана — сохраняем её с полученным ID
        if (image != null && !image.isEmpty()) {
            String imagePath = imageService.saveImage(image, "ads", ad.getId());
            ad.setImagePath(imagePath);
            ad = adRepository.save(ad);
        }

        log.info("Создано объявление: id={}, автор={}", ad.getId(), currentUser.getUsername());
        return adMapper.toDto(ad);
    }

    /**
     * Обновляет объявление. PATCH /ads/{id}. Только автор или админ.
     *
     * @param id        ID объявления
     * @param updateReq новые данные
     * @return обновлённое DTO
     * @throws NotFoundException  если объявление не найдено/удалено (→ 404)
     * @throws ForbiddenException если нет прав (→ 403)
     */
    @Transactional
    public AdDto updateAd(Long id, UpdateAdReq updateReq) {
        Ad ad = findActiveAd(id);
        checkPermission(ad);

        adMapper.updateAdFromDto(updateReq, ad);
        ad.setUpdatedAt(LocalDateTime.now());

        ad = adRepository.save(ad);
        log.info("Обновлено объявление: id={}", id);
        return adMapper.toDto(ad);
    }

    /**
     * Мягко удаляет объявление. DELETE /ads/{id}. Только автор или админ.
     *
     * @param id ID объявления
     * @throws NotFoundException  если объявление не найдено/удалено (→ 404)
     * @throws ForbiddenException если нет прав (→ 403)
     */
    @Transactional
    public void deleteAd(Long id) {
        Ad ad = findActiveAd(id);
        checkPermission(ad);

        ad.setActive(false);
        adRepository.save(ad);
        log.info("Удалено объявление (мягкое удаление): id={}", id);
    }

    /**
     * Обновляет изображение объявления. PATCH /ads/{id}/image.
     *
     * 🆕 После успешной замены СТАРАЯ картинка удаляется с диска
     * (замечание наставника). Тип файла валидируется в ImageService.
     *
     * @param id    ID объявления
     * @param image новый файл картинки
     * @return URL-путь новой картинки
     * @throws IOException           если не удалось записать файл
     * @throws NotFoundException     если объявление не найдено/удалено (→ 404)
     * @throws ForbiddenException    если нет прав (→ 403)
     * @throws InvalidImageException если файл не является картинкой (→ 400)
     */
    @Transactional
    public String updateAdImage(Long id, MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Файл изображения пуст");
        }

        Ad ad = findActiveAd(id);
        checkPermission(ad);

        String oldPath = ad.getImagePath();

        String imagePath = imageService.saveImage(image, "ads", id);
        ad.setImagePath(imagePath);
        ad.setUpdatedAt(LocalDateTime.now());
        adRepository.save(ad);

        // Удаляем старый файл ПОСЛЕ успешного сохранения нового
        imageService.deleteImage(oldPath);

        log.info("Обновлена картинка объявления: id={}", id);
        return imagePath;
    }

    /**
     * Находит АКТИВНОЕ объявление по ID — единая точка проверки активности.
     *
     * @param id ID объявления
     * @return сущность объявления
     * @throws NotFoundException если объявления нет или оно мягко удалено
     */
    private Ad findActiveAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Объявление с id=" + id + " не найдено"));

        if (!ad.isActive()) {
            throw new NotFoundException("Объявление с id=" + id + " удалено");
        }
        return ad;
    }

    /**
     * Проверяет права на объявление: текущий пользователь — автор или ADMIN.
     *
     * @param ad объявление
     * @throws ForbiddenException если прав нет
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
