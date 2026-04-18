package Zvonok.features.auth.jwt.refreshToken.refreshTokenService;

import Zvonok.core.common.exception.customException.refreshTokenException.RefreshTokenNotFoundException;
import Zvonok.core.common.exception.customException.userException.UserAlreadyExistsException;
import Zvonok.core.common.exception.customException.userException.UserNotFoundException;
import Zvonok.features.auth.jwt.refreshToken.redisRefreshTokenService.RedisRefreshTokenService;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RedisRefreshTokenService redisRefreshTokenService;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    // ================= ОСНОВНЫЕ МЕТОДЫ =================

    @Transactional
    public String createRefreshToken(User user, HttpServletResponse response) {

        log.info("--- Создание Refresh-токена для пользователя: {} ---", user.getEmail());

        validateUserForRefresh(user, response);

        try {
            if (refreshExpirationMs <= 0) {
                throw new IllegalStateException("Время жизни refresh-токена должно быть положительным");
            }

            String tokenString = UUID.randomUUID().toString();
            long ttlRefresh = refreshExpirationMs;

            redisRefreshTokenService.savedRefreshToken(tokenString, user.getId(), ttlRefresh);

            log.info("=== Новый Refresh-токен создан для пользователя {} ===", user.getId(),tokenString,ttlRefresh);

            return tokenString;

        } catch (Exception e) {
            log.error("!!! НЕ УДАЛОСЬ создать refresh-токен !!!", e);
            throw e;
        }
    }

    // ================= ПРОВЕРКА ТОКЕНА =================
    @Transactional(readOnly = true)
    public Long validateRefreshToken(String token) {
        log.info("Валидация рефрешьТокена: {}", maskToken(token));

        if (token == null || token.trim().isEmpty()) {
            log.error("РефрешьТокен пустой ");
            throw new RefreshTokenNotFoundException();
        }

        Long userId = redisRefreshTokenService.getUserIdByRefreshToken(token);

        if (userId == null) {
            log.error("Refresh token не найден или истёк: {}", maskToken(token));
            throw new RefreshTokenNotFoundException();
        }

        log.info("Refresh token валиден для userId: {}", userId);

        return userId;
    }

    // ================= ОБНОВЛЕНИЕ ТОКЕНА =================
    @Transactional
    public String rotateRefreshToken(String oldRefreshToken, User user, HttpServletResponse response) {
        log.info("Попытка обновить RefreshToken");

        if (oldRefreshToken == null || oldRefreshToken.trim().isEmpty()) {
            log.error("Refresh token пустой для ротации");
            throw new RefreshTokenNotFoundException();
        }

        try {
            deleteRefreshToken(oldRefreshToken,user.getId());

            String newRefreshToken = createRefreshToken(user, response);
            log.info("Ротация refresh токена успешна для пользователя: {}", user.getEmail());

            return newRefreshToken;

        } catch (Exception e) {
            log.error("Ошибка обновления токена: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ================= УДАЛЕНИЕ ТОКЕНА =================
    @Transactional
    public void deleteRefreshToken(String token,Long userId) {
        log.info("Удаление рефрешь ткоена: {}", maskToken(token));

        if (token == null || token.trim().isEmpty()) {
            log.warn("Токен для удаления пустой");
            return;
        }
        redisRefreshTokenService.deleteRefreshToken(token,userId);
    }

    @Transactional
    public void deleteAllRefreshTokensByUserId(Long userId) {
        redisRefreshTokenService.deleteAllUserRefreshTokens(userId);
    }


    // ================= МЕТОДЫ ДЛЯ РАБОТЫ С COOKIE =================

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {

        log.info("Установка cookie для refresh-токена");

        Cookie refreshCookie = new Cookie("refreshToken", token);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) (refreshExpirationMs / 1000));
        refreshCookie.setAttribute("SameSite", "Lax");

        response.addCookie(refreshCookie);

        log.info("Cookie для refresh-токена установлено. Путь: {}, Срок жизни: {} секунд",
                refreshCookie.getPath(), refreshCookie.getMaxAge());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        log.info("Clearing refresh token cookie");

        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setHttpOnly(true);
        deleteCookie.setSecure(false);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);

        response.addCookie(deleteCookie);
        log.info("Refresh token cookie очищен");
    }

    // ================= МЕТОДЫ ДЛЯ ИЗВЛЕЧЕНИЯ ТОКЕНА ИЗ ЗАПРОСА =================

    public Optional<String> extractRefreshTokenFromRequest(HttpServletRequest request) {
        log.debug("Достаю рефрешь из куки");

        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue);
        }

        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }

        return Optional.empty();
    }

    // ================= ОЧИСТКА ПРОСРОЧЕННЫХ ТОКЕНОВ =================



    // ================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =================

    // -------------------------------------------------------------------- прячу токен в логах

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "****";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    // -------------------------------------------------------------------- валидцаия входных параметров
    // -------------------------------------------------------------------- при создание рефрешь токена

    private void validateUserForRefresh(User user, HttpServletResponse response) {

        if (user == null) {
            log.error("Пользователь пустой");
            throw new UserAlreadyExistsException("Пользователь не найден");
        }

        if (user.getId() == null) {
            log.error("ID пользователя отсутствует для: {}", user.getEmail());
            throw new UserAlreadyExistsException("ID пользователя не может быть null");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("Email пустой для пользователя с ID: {}", user.getId());
            throw new UserAlreadyExistsException("Email пользователя не найден");
        }

        if (response == null) {
            log.error("HttpServletResponse пустой");
            throw new IllegalArgumentException("HttpServletResponse не может быть пустым");
        }

        if (!userRepository.existsById(user.getId())) {
            log.error("Пользователь с ID {} отсутствует в базе данных", user.getId());
            throw new UserNotFoundException();
        }


    }
}