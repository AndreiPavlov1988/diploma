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
 * 🆕 Все операции учитывают признак активности (soft-delete):
 * - списки возвращают только активные объявления;
 * - изменение/удаление удалённого объявления → 404.
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
     * Получить ВСЕ активные объявления (новые сверху).
     * GET /ads — доступен без авторизации.
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
     * 🆕 Получить АКТИВНЫЕ объявления текущего пользователя.
     * GET /ads/me — удалённые объявления больше не показываются в профиле.
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
     * Получить активное объявление по ID с полной информацией.
     * GET /ads/{id} — доступен без авторизации.
     *
     * @throws NotFoundException если объявление не найдено или удалено (→ 404)
     */
    public FullAdDto getAdById(Long id) {
        Ad ad = findActiveAd(id);
        return adMapper.toFullAdDto(ad);
    }

    /**
     * Создать новое объявление (возможно с изображением).
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

        // Если есть картинка — сохраняем её с полученным ID
        if (image != null && !image.isEmpty()) {
            String imagePath = imageService.saveImage(image, "ads", ad.getId());
            ad.setImagePath(imagePath);
            ad = adRepository.save(ad);
        }

        log.info("Создано объявление: id={}, автор={}", ad.getId(), currentUser.getUsername());
        return adMapper.toDto(ad);
    }

    /**
     * Обновить объявление (только автор или администратор).
     * 🆕 Обновление удалённого объявления → 404.
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
     * Удалить объявление (мягкое удаление, только автор или администратор).
     * 🆕 Повторное удаление уже удалённого → 404.
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
     * Обновить изображение объявления (только автор или администратор).
     */
    @Transactional
    public String updateAdImage(Long id, MultipartFile image) throws IOException {
        Ad ad = findActiveAd(id);
        checkPermission(ad);

        String imagePath = imageService.saveImage(image, "ads", id);
        ad.setImagePath(imagePath);
        ad.setUpdatedAt(LocalDateTime.now());
        adRepository.save(ad);

        log.info("Обновлена картинка объявления: id={}", id);
        return imagePath;
    }

    /**
     * 🆕 Найти АКТИВНОЕ объявление по ID.
     * Если объявления нет или оно мягко удалено — 404.
     * Единая точка проверки активности для всех операций.
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
     * Проверка прав доступа к объявлению (автор или администратор).
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
