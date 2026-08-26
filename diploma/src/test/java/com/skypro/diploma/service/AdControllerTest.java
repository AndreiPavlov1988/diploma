package com.skypro.diploma.controller;

import com.skypro.diploma.dto.ad.AdsDto;
import com.skypro.diploma.dto.ad.FullAdDto;
import com.skypro.diploma.security.SecurityConfig;
import com.skypro.diploma.service.AdService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты контроллера объявлений.
 * Проверяют публичный доступ и порядок маршрутов (/ads/me ПЕРЕД /ads/{id}).
 */
@WebMvcTest(AdController.class)
@Import(SecurityConfig.class)
class AdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdService adService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void getAds_withoutAuth_returns200() throws Exception {
        AdsDto dto = new AdsDto();
        dto.setCount(0);
        dto.setResults(List.of());
        when(adService.getAllAds()).thenReturn(dto);

        // просмотр объявлений открыт всем
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void getAds_withInvalidBasicHeader_stillReturns200() throws Exception {
        AdsDto dto = new AdsDto();
        dto.setCount(0);
        dto.setResults(List.of());
        when(adService.getAllAds()).thenReturn(dto);
        // невалидные credentials не должны ломать публичный эндпоинт
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(get("/ads")
                        .header(HttpHeaders.AUTHORIZATION, "Basic aW52YWxpZDp3cm9uZw=="))
                .andExpect(status().isOk());
    }

    @Test
    void getAdById_withoutAuth_returns200() throws Exception {
        FullAdDto dto = new FullAdDto();
        dto.setId(1L);
        dto.setTitle("iPhone 15");
        when(adService.getAdById(1L)).thenReturn(dto);

        mockMvc.perform(get("/ads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("iPhone 15"));
    }

    @Test
    void getMyAds_withoutAuth_returns401() throws Exception {
        // /ads/me защищён и НЕ перехватывается маршрутом /ads/{id}
        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isUnauthorized());
    }
}
