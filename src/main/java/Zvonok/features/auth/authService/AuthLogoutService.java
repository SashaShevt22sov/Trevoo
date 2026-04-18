package Zvonok.features.auth.authService;

import Zvonok.features.auth.authDto.logout.LogoutResponseDto;
import Zvonok.features.auth.jwt.refreshToken.refreshTokenService.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthLogoutService {

    private final RefreshTokenService refreshTokenService;

    // =============================== ВЫХОД ИЗ АККАУНТА
    public ResponseEntity<LogoutResponseDto> logout(HttpServletRequest request, HttpServletResponse response,Long userId) {
        try {
            Optional<String> tokenOpt = refreshTokenService.extractRefreshTokenFromRequest(request);

            if (tokenOpt.isEmpty()) {
                log.info("Logout запрос без refresh token - возможно пользователь уже вышел");

                refreshTokenService.clearRefreshTokenCookie(response);

                return ResponseEntity.ok()
                        .body(new LogoutResponseDto("Выход выполнен успешно"));
            }

            String refreshToken = tokenOpt.get();
            refreshTokenService.deleteRefreshToken(refreshToken,userId);
            refreshTokenService.clearRefreshTokenCookie(response);

            log.info("Пользователь успешно вышел из системы");
            return ResponseEntity.ok()
                    .body(new LogoutResponseDto("Успешный выход из системы"));

        } catch (Exception e) {
            log.error("Ошибка при выходе из системы: {}", e.getMessage());

            refreshTokenService.clearRefreshTokenCookie(response);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new LogoutResponseDto("Ошибка при выходе из системы"));
        }
    }

}
