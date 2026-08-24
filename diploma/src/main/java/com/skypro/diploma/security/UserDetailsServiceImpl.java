package com.skypro.diploma.security;

import com.skypro.diploma.entity.User;
import com.skypro.diploma.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Собственная реализация {@link UserDetailsService} (требование ТЗ).
 *
 * Spring Security вызывает метод loadUserByUsername() каждый раз,
 * когда пользователь пытается войти (Basic Auth).
 * Мы ищем пользователя в БД через UserRepository и возвращаем его
 * в формате, который понимает Spring Security.
 */
@Service  // регистрируем класс как Bean Spring
@RequiredArgsConstructor  // Lombok создает конструктор с полем userRepository
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;  // репозиторий для доступа к таблице users

    /**
     * Загружает пользователя из БД по его email (username).
     *
     * @param username email пользователя из заголовка Authorization
     * @return объект UserDetails, который понимает Spring Security
     * @throws UsernameNotFoundException если пользователя с таким email нет в БД
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Ищем пользователя в таблице users по email
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Пользователь с email " + username + " не найден"));

        // 2. Превращаем нашу Entity в объект Spring Security
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),   // логин (email)
                user.getPassword(),   // пароль (хранится в БД в виде BCrypt-хэша)
                user.isActive(),      // активен ли аккаунт (поле is_active)
                true,                 // аккаунт не просрочен
                true,                 // учетные данные не просрочены
                true,                 // аккаунт не заблокирован
                // 3. Роли пользователя: USER -> ROLE_USER, ADMIN -> ROLE_ADMIN
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }
}
