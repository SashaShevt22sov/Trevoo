package Zvonok.jwt.refreshToken.refreshTokenService;

import Zvonok.common.exception.customException.userException.InvalidPasswordException;
import Zvonok.common.exception.customException.userException.UserAlreadyExistsException;
import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.jwt.accessToken.JwtAccessTokenService;
import Zvonok.jwt.refreshToken.entity.RefreshToken;
import Zvonok.jwt.refreshToken.refreshTokenDto.TokenRefreshSilentRefreshResponseDto;
import Zvonok.jwt.refreshToken.refreshTokenRepository.RefreshTokenRepository;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final UserRepository userRepository;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    // ================= ОСНОВНЫЕ МЕТОДЫ =================

    @Transactional
    public RefreshToken createRefreshToken(User user, HttpServletResponse response) {

        log.info("--- Создание Refresh-токена для пользователя: {} ---", user.getEmail());

        validateUserForRefresh(user, response);

        try {


            log.info("Создаём новый refresh-токен (старые сессии остаются активными)");

            // Проверка срока жизни
            if (refreshExpirationMs <= 0) {
                throw new IllegalStateException("Время жизни refresh-токена должно быть положительным");
            }


            String tokenString = UUID.randomUUID().toString();

            Instant expiryDate = Instant.now().plusMillis(refreshExpirationMs);

            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(tokenString)
                    .expiryDate(expiryDate)
                    .revoked(false)
                    .build();

            RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

            // Установка cookie
            setRefreshTokenCookie(response, tokenString);

            log.info("=== Новый Refresh-токен создан (ID: {}, expires: {}) ===",
                    savedToken.getId(), expiryDate);

            return savedToken;

        } catch (Exception e) {
            log.error("!!! НЕ УДАЛОСЬ создать refresh-токен !!!", e);
            throw e;
        }
    }

    // ================= ПРОВЕРКА ТОКЕНА =================
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        log.info("Валидация рефрешьТокена: {}", maskToken(token));

        if (token == null || token.trim().isEmpty()) {
            log.error("рефрешьТокен пустой ");
            throw new InvalidPasswordException("Refresh token не может быть пустым");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    log.error("Рефрешь токен не найден в базе данных : {}", maskToken(token));
                    return new InvalidPasswordException("Refresh token не найден");
                });

        log.info("Found token: ID={}, User={}, Expiry={}, Revoked={}",
                refreshToken.getId(),
                refreshToken.getUser().getEmail(),
                refreshToken.getExpiryDate(),
                refreshToken.isRevoked());

        if (!refreshToken.isValid()) {
            log.error("Token is invalid - Expired or Revoked");
            log.error("Token valid: {}, Revoked: {}, Expired: {}",
                    refreshToken.isValid(),
                    refreshToken.isRevoked(),
                    refreshToken.getExpiryDate().isBefore(Instant.now()));
            throw new InvalidPasswordException("Refresh token недействителен или истек");
        }

        log.info("Refresh token is valid for user: {}", refreshToken.getUser().getEmail());
        return refreshToken;
    }

    // ================= ОБНОВЛЕНИЕ ТОКЕНА =================
    @Transactional
    public TokenRefreshSilentRefreshResponseDto refreshAccessToken(String refreshToken, HttpServletResponse response) {
        log.info("Попытка обновления access token");

        try {
            RefreshToken validatedToken = validateRefreshToken(refreshToken);
            User user = validatedToken.getUser();

            String newAccessToken = jwtAccessTokenService.generateAccessToken(user);


            return TokenRefreshSilentRefreshResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .build();

        } catch (Exception e) {
            log.error("Ошибка обновления токена: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ================= УДАЛЕНИЕ ТОКЕНА =================
    @Transactional
    public void deleteRefreshToken(String token) {
        log.info("Deleting refresh token: {}", maskToken(token));

        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to delete null or empty token");
            return;
        }

        refreshTokenRepository.findByToken(token).ifPresentOrElse(
                refreshToken -> {
                    refreshTokenRepository.delete(refreshToken);
                    log.info("Refresh token deleted successfully for user: {}",
                            refreshToken.getUser().getEmail());
                },
                () -> log.warn("Refresh token not found for deletion: {}", maskToken(token))
        );
    }

    @Transactional
    public void deleteAllUserTokens(User user) {
        log.info("Deleting all refresh tokens for user: {}", user.getEmail());

        if (user == null || user.getId() == null) {
            log.error("Cannot delete tokens for null user");
            throw new IllegalArgumentException("User cannot be null");
        }

        int deletedCount = refreshTokenRepository.deleteByUser(user);
        log.info("Deleted {} refresh tokens for user: {}", deletedCount, user.getEmail());
    }

    // ================= МЕТОДЫ ДЛЯ РАБОТЫ С COOKIE =================

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {

        log.info("Установка cookie для refresh-токена");

        Cookie refreshCookie = new Cookie("refreshToken", token);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false); // для разработки false, в продакшене true
        refreshCookie.setPath("/"); // ограничиваем путь cookie
        refreshCookie.setMaxAge((int) (refreshExpirationMs / 1000)); // переводим миллисекунды в секунды
        refreshCookie.setAttribute("SameSite", "Lax"); // Защита от CSRF

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
        log.info("Refresh token cookie cleared");
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

        // Пробуем получить из заголовка (как fallback)
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }

        return Optional.empty();
    }

    // ================= ОЧИСТКА ПРОСРОЧЕННЫХ ТОКЕНОВ =================

    @Transactional
    public int cleanupExpiredTokens() {
        log.info("Starting cleanup of expired refresh tokens");

        List<RefreshToken> expiredTokens = refreshTokenRepository.findAll().stream()
                .filter(token -> token.getExpiryDate().isBefore(Instant.now()))
                .toList();

        if (!expiredTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(expiredTokens);
            log.info("Cleaned up {} expired refresh tokens", expiredTokens.size());
        } else {
            log.info("No expired tokens found");
        }

        return expiredTokens.size();
    }

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
            throw new UserNotFoundException("Пользователь не найден в базе данных");
        }

        // -------------------------------------------------------------------- генерация рефрешь токена
    }
}