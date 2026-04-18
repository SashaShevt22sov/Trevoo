package Zvonok.features.auth.authService;

import Zvonok.features.auth.authDto.login.LoginRequestDto;
import Zvonok.features.auth.authDto.login.LoginResponseDto;
import Zvonok.features.auth.jwt.accessToken.JwtAccessTokenService;
import Zvonok.features.auth.jwt.refreshToken.refreshTokenService.RefreshTokenService;
import Zvonok.core.common.exception.customException.otpException.VerificationExpiredException;
import Zvonok.core.common.exception.customException.userException.UserInvalidCredentialsException;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLoginService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto, HttpServletResponse response) {

        String email = loginRequestDto.getEmail().trim().toLowerCase();
        String password = loginRequestDto.getPassword();

        log.info("Попытка логина: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", email);
                    return new UserInvalidCredentialsException("Неверный email или пароль");
                });


        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Неверный пароль для пользователя: {}", email);
            throw new UserInvalidCredentialsException("Неверный email или пароль");
        }


        if (!user.isRegisterVerify()) {
            log.warn("Попытка входа непроверенным пользователем: {}", email);
            throw new VerificationExpiredException();
        }


        String accessToken = jwtAccessTokenService.generateAccessToken(user);
        String newRefreshToken = refreshTokenService.createRefreshToken(user, response);
        refreshTokenService.setRefreshTokenCookie(response, newRefreshToken);

        log.info("Успешный вход: {}", email);

        return LoginResponseDto.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

}
