package com.skypro.diploma.controller;

import com.skypro.diploma.dto.auth.LoginResp;
import com.skypro.diploma.exception.ConflictException;
import com.skypro.diploma.security.SecurityConfig;
import com.skypro.diploma.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты контроллера аутентификации.
 * @WebMvcTest поднимает только веб-слой + MockMvc (без реального сервера и БД).
 * @Import(SecurityConfig.class) — чтобы работали реальные правила безопасности.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void register_success_returns201() throws Exception {
        String body = """
                {"username":"test@mail.ru","password":"password123",
                 "firstName":"Иван","lastName":"Иванов",
                 "phone":"+7 999 123-45-67","role":"USER"}
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        doThrow(new ConflictException("Пользователь с таким email уже существует"))
                .when(userService).register(any());

        String body = """
                {"username":"test@mail.ru","password":"password123",
                 "firstName":"Иван","lastName":"Иванов",
                 "phone":"+7 999 123-45-67","role":"USER"}
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void login_success_returnsUserDataWithoutToken() throws Exception {
        when(userService.authenticateAndBuildResponse("test@mail.ru"))
                .thenReturn(new LoginResp(1L, "test@mail.ru", "Иван", "Иванов", "USER"));

        String body = """
                {"username":"test@mail.ru","password":"password123"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("test@mail.ru"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
