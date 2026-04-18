package Zvonok.features.auth.authService;

import Zvonok.features.auth.authDto.resetPassword.ConfirmResetPasswordRequestDto;
import Zvonok.features.auth.authDto.resetPassword.ConfirmResetPasswordResponseDto;
import Zvonok.features.auth.authDto.resetPassword.ResetPasswordRequestDto;
import Zvonok.features.auth.authDto.resetPassword.ResetPasswordResponseDto;
import Zvonok.features.auth.jwt.refreshToken.refreshTokenService.RefreshTokenService;
import Zvonok.infrastructure.email.EmailService;
import Zvonok.features.auth.passwordResetToken.entity.PasswordResetToken;
import Zvonok.features.auth.passwordResetToken.passwordResetRepository.PasswordResetTokenRepository;
import Zvonok.features.auth.passwordResetToken.passwordResetService.PasswordResetTokenService;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthPasswordResetService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    // =============================== ЗАПРОС НА СБРОС ПАРОЛЯ (ОТПРАВКА EMAIL)

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto request) {

        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return successResponseSendEmail();
        }

        User user = userOpt.get();

        if (!user.isRegisterVerify()) {
            return successResponseSendEmail();
        }

        passwordResetTokenRepository.deleteByUser(user);

        String tokenValue = passwordResetTokenService.generateSecureToken();
        PasswordResetToken resetToken = new PasswordResetToken(user, tokenValue);
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:5173/auth/reset-password?token=" + tokenValue;

        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        } catch (Exception e) {
            log.error("Не удалось отправить письмо восстановления на {}", email, e);
        }

        return successResponseSendEmail();
    }

    // =============================== ПОДТВЕРЖДЕНИЕ СБРОСА ПАРОЛЯ
    @Transactional
    public ConfirmResetPasswordResponseDto confirmResetPassword(ConfirmResetPasswordRequestDto request) {

        String newPassword = request.getNewPassword();
        String resetToken = request.getResetToken();

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(resetToken);

        if (tokenOpt.isEmpty()) {
            return responseConfirmResetPassword("Неверный или просроченный токен", false);
        }

        PasswordResetToken token = tokenOpt.get();

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            return responseConfirmResetPassword("Токен истек", false);
        }

        User user = token.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(token);
        refreshTokenService.deleteAllRefreshTokensByUserId(user.getId());


        return responseConfirmResetPassword("Пароль успешно обновлен. Все активные сессии завершены.", true);
    }


    /// =================================== Help Методы ====================================///

    private ResetPasswordResponseDto successResponseSendEmail() {
        return ResetPasswordResponseDto.builder()
                .success(true)
                .message("Мы отправили письмо на почту которую вы указали")
                .build();
    }

    private ConfirmResetPasswordResponseDto responseConfirmResetPassword(String message, boolean success) {
        return ConfirmResetPasswordResponseDto.builder()
                .success(success)
                .message(message)
                .build();
    }
}
