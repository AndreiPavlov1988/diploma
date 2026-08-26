package com.skypro.diploma.service;

import com.skypro.diploma.dto.ad.AdDto;
import com.skypro.diploma.dto.ad.AdsDto;
import com.skypro.diploma.entity.Ad;
import com.skypro.diploma.entity.User;
import com.skypro.diploma.enums.Role;
import com.skypro.diploma.exception.ForbiddenException;
import com.skypro.diploma.exception.NotFoundException;
import com.skypro.diploma.mapper.AdMapper;
import com.skypro.diploma.repository.AdRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit-тесты AdService.
 * Главное: разграничение прав (автор / чужой / админ) и soft-delete.
 */
@ExtendWith(MockitoExtension.class)
class AdServiceTest {

    @Mock
    private AdRepository adRepository;
    @Mock
    private AdMapper adMapper;
    @Mock
    private UserService userService;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private AdService adService;

    // ===== Вспомогательные методы-фабрики =====

    private User user(Long id, Role role) {
        User u = new User();
        u.setId(id);
        u.setRole(role);
        u.setUsername("user" + id + "@mail.ru");
        return u;
    }

    private Ad ad(Long id, User author, boolean active) {
        Ad a = new Ad();
        a.setId(id);
        a.setAuthor(author);
        a.setActive(active);
        a.setTitle("Объявление " + id);
        return a;
    }

    // ===== Тесты =====

    @Test
    void getAllAds_returnsOnlyActive() {
        Ad active = ad(1L, user(1L, Role.USER), true);
        when(adRepository.findAllByActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(active));
        when(adMapper.toDtoList(List.of(active))).thenReturn(List.of(new AdDto()));

        AdsDto result = adService.getAllAds();

        assertThat(result.getCount()).isEqualTo(1);
    }

    @Test
    void getAdById_whenInactive_throwsNotFound() {
        when(adRepository.findById(1L))
                .thenReturn(Optional.of(ad(1L, user(1L, Role.USER), false)));

        // удалённое объявление -> 404
        assertThatThrownBy(() -> adService.getAdById(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getMyAds_usesActiveOnlyFilter() {
        User me = user(1L, Role.USER);
        when(userService.getCurrentUser()).thenReturn(me);
        when(adRepository.findAllByAuthorIdAndActiveTrue(1L)).thenReturn(List.of());

        AdsDto result = adService.getMyAds();

        // в профиле только активные объявления
        assertThat(result.getCount()).isEqualTo(0);
        verify(adRepository).findAllByAuthorIdAndActiveTrue(1L);
    }

    @Test
    void deleteAd_byNonAuthorNonAdmin_throwsForbidden() {
        Ad ad = ad(1L, user(1L, Role.USER), true);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(userService.getCurrentUser()).thenReturn(user(2L, Role.USER)); // чужой пользователь

        // чужой пользователь не может удалить -> 403
        assertThatThrownBy(() -> adService.deleteAd(1L))
                .isInstanceOf(ForbiddenException.class);

        // объявление осталось активным
        assertThat(ad.isActive()).isTrue();
    }

    @Test
    void deleteAd_byAuthor_softDeletes() {
        User author = user(1L, Role.USER);
        Ad ad = ad(1L, author, true);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(userService.getCurrentUser()).thenReturn(author);

        adService.deleteAd(1L);

        // автор удалил: запись в БД осталась, но is_active = false
        assertThat(ad.isActive()).isFalse();
        verify(adRepository).save(ad);
    }

    @Test
    void deleteAd_byAdmin_softDeletes() {
        Ad ad = ad(1L, user(1L, Role.USER), true);
        when(adRepository.findById(1L)).thenReturn(Optional.of(ad));
        when(userService.getCurrentUser()).thenReturn(user(9L, Role.ADMIN)); // админ

        adService.deleteAd(1L);

        // админ может удалять любые объявления
        assertThat(ad.isActive()).isFalse();
    }
}
