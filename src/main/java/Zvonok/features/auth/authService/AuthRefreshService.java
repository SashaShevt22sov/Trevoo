package Zvonok.features.auth.authService;

import Zvonok.features.auth.authDto.refreshToken.TokenRefreshResponseDto;
import Zvonok.features.auth.jwt.accessToken.JwtAccessTokenService;
import Zvonok.features.auth.jwt.refreshToken.refreshTokenService.RefreshTokenService;
import Zvonok.core.common.exception.customException.refreshTokenException.RefreshTokenNotFoundException;
import Zvonok.core.common.exception.customException.userException.UserNotFoundException;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthRefreshService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtAccessTokenService jwtAccessTokenService;

    // =============================== ВОССТАНОВЛЕНИЯ АВТОРИЗАЦИИ

    public TokenRefreshResponseDto refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refresh сервис : {}", Thread.currentThread());
        String refreshToken = refreshTokenService.extractRefreshTokenFromRequest(request)
                .orElseThrow(RefreshTokenNotFoundException::new);

        Long userId = refreshTokenService.validateRefreshToken(refreshToken);

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken, user, response);

        String newAccessToken = jwtAccessTokenService.generateAccessToken(user);
        refreshTokenService.setRefreshTokenCookie(response, newRefreshToken);

        return TokenRefreshResponseDto.builder()
                .username(user.getUsername())
                .accessToken(newAccessToken)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
