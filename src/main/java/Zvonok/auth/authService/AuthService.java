package Zvonok.auth.authService;

import Zvonok.auth.authDto.*;
import Zvonok.auth.authDto.login.LoginRequestDto;
import Zvonok.auth.authDto.login.LoginResponseDto;
import Zvonok.auth.authDto.logout.LogoutResponseDto;
import Zvonok.auth.authDto.refreshToken.TokenRefreshResponseDto;
import Zvonok.auth.authDto.resetPassword.ConfirmResetPasswordRequestDto;
import Zvonok.auth.authDto.resetPassword.ConfirmResetPasswordResponseDto;
import Zvonok.auth.authDto.resetPassword.ResetPasswordRequestDto;
import Zvonok.auth.authDto.resetPassword.ResetPasswordResponseDto;
import Zvonok.auth.entity.OtpVerificationData;
import Zvonok.auth.entity.UserRegistrationData;
import Zvonok.common.Enum.Role;
import Zvonok.common.exception.customException.otpException.InvalidOtpCodeException;
import Zvonok.common.exception.customException.otpException.OtpAttemptsExceededException;
import Zvonok.common.exception.customException.otpException.TooManyRequestsException;
import Zvonok.common.exception.customException.refreshTokenException.RefreshTokenNotFoundException;
import Zvonok.common.exception.customException.userException.InvalidCredentialsException;
import Zvonok.common.exception.customException.userException.UserAlreadyExistsException;
import Zvonok.common.exception.customException.otpException.VerificationExpiredException;
import Zvonok.common.successrResponse.SuccessResponse;
import Zvonok.email.EmailService;
import Zvonok.jwt.accessToken.JwtAccessTokenService;
import Zvonok.jwt.refreshToken.entity.RefreshToken;
import Zvonok.jwt.refreshToken.refreshTokenService.RefreshTokenService;
import Zvonok.otp.OtpService;
import Zvonok.passwordResetToken.entity.PasswordResetToken;
import Zvonok.passwordResetToken.passwordResetRepository.PasswordResetTokenRepository;
import Zvonok.passwordResetToken.passwordResetService.PasswordResetTokenService;
import Zvonok.redis.RedisService;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {



    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final RedisService redisService;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;


    private static final String VERIFICATION_PREFIX = "verification:";
    private static final String PENDING_EMAIL_KEY = "pending:reg:email:";
    private static final int OTP_TTL_SECONDS = 600;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_RESEND_ATTEMPTS = 5;

    @Value("${spring.storage.default-avatar}")
    private  String DEFAULT_URL_AVATAR;

    // =============================== РЕГИСТРАЦИЯ

    @Transactional
    public VerificationResponseDto registerNewUser(RegisterRequestDto dto) {

        String email = dto.getEmail().trim().toLowerCase();
        String username = dto.getUsername().trim();

        log.info("Попытка регистрации: email={}, username={}", email, username);

        if (userRepository.existsByEmail(email)) {
            log.info("Регистрация отклонена: email уже существует и подтверждён: {}", email);
            throw new UserAlreadyExistsException("Email уже зарегистрирован и подтверждён");
        }
        if (userRepository.existsByUsername(username)) {
            log.info("Регистрация отклонена: username уже занят: {}", username);
            throw new UserAlreadyExistsException("Username уже занят");
        }

        String pendingKey = PENDING_EMAIL_KEY + email;

        if (redisService.exists(pendingKey)) {

            String oldVerificationId = redisService.get(pendingKey).toString();
            String oldVerificationKey = VERIFICATION_PREFIX + oldVerificationId;

            redisService.delete(oldVerificationKey);
            log.info("Удалена старая незавершённая регистрация для email: {}, verificationId: {}", email, oldVerificationId);

        }

        UserRegistrationData userData = UserRegistrationData.builder()
                .email(email)
                .username(username)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .build();

        OtpVerificationData otpData = OtpVerificationData.builder()
                .otpCode(otpService.generateOtp())
                .attempts(0)
                .maxAttempts(MAX_OTP_ATTEMPTS)
                .build();

        PendingRegistrationDto pending = PendingRegistrationDto.builder()
                .userData(userData)
                .verification(otpData)
                .build();

        String verificationId = UUID.randomUUID().toString();

        String verificationKey = VERIFICATION_PREFIX + verificationId;

        redisService.set(verificationKey, pending, OTP_TTL_SECONDS);

        redisService.set(PENDING_EMAIL_KEY + email, verificationId, OTP_TTL_SECONDS);

        emailService.sendOtpEmail(email, otpData.getOtpCode());

        log.info("Отправлен OTP на {} | verificationId: {}", email, verificationId);

        return new VerificationResponseDto(verificationId, true,
                "Проверьте email для подтверждения регистрации");
    }

    // =============================== ПОДТВЕРЖДЕНИЕ РЕГИСТРАЦИИ

    @Transactional
    public RegisterResponseDto verifyRegisterNewUser(VerificationRequestDto req, HttpServletResponse response) {

        String verificationKey = VERIFICATION_PREFIX + req.getVerificationId();
        PendingRegistrationDto data = (PendingRegistrationDto) redisService.get(verificationKey);

        if (data == null) {
            log.info("Ошибка верификации: verificationId={} не найден или истёк",
                    req.getVerificationId());
            throw new VerificationExpiredException("Срок действия кода истёк. Пожалуйста, запросите новый код.");
        }

        String email = data.getUserData().getEmail();
        OtpVerificationData otpData = data.getVerification();

        if (!otpData.getOtpCode().equals(req.getOtpCode())) {

            int newAttempts = otpData.getAttempts() + 1;
            otpData.setAttempts(newAttempts);

            redisService.set(verificationKey, data, OTP_TTL_SECONDS);

            log.info("Неверный OTP: email={}, attempts={}/{}",
                    email, newAttempts, otpData.getMaxAttempts());

            if (newAttempts >= otpData.getMaxAttempts()) {
                redisService.delete(verificationKey);
                redisService.delete(PENDING_EMAIL_KEY + email);

                log.info("Превышено количество попыток OTP: email={}", email);

                throw new OtpAttemptsExceededException("Слишком много неверных попыток. Попробуйте зарегистрироваться снова.");
            }

            throw new InvalidOtpCodeException("Неверный код. Осталось попыток: " + (otpData.getMaxAttempts() - newAttempts));
        }

        UserRegistrationData regData = data.getUserData();

        final User newUser = User.builder()
                .email(regData.getEmail())
                .username(regData.getUsername())
                .password(regData.getPasswordHash())
                .roles(Set.of(Role.ROLE_USER))
                .createdAt(LocalDateTime.now())
                .registerVerify(true)
                .avatarUrl(DEFAULT_URL_AVATAR)
                .build();

        User savedUser = userRepository.save(newUser);

        log.info("Успешная регистрация и верификация: {}", savedUser.getEmail());

        redisService.delete(verificationKey);
        redisService.delete(PENDING_EMAIL_KEY + regData.getEmail());

        String accessToken = jwtAccessTokenService.generateAccessToken(savedUser);
        refreshTokenService.createRefreshToken(savedUser, response);

        return RegisterResponseDto.builder()
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .accessToken(accessToken)
                .avatarUrl(savedUser.getAvatarUrl())
                .build();
    }

    // =============================== ПОВТОРНАЯ ОТПРАВКА ОТП КОДА

    @Transactional
    public ResendOtpResponseDto resendNewOtp(ResendOtpRequestDto req) {

        String verificationKey = VERIFICATION_PREFIX + req.getVerificationId();
        PendingRegistrationDto data = (PendingRegistrationDto) redisService.get(verificationKey);

        if (data == null) {
            throw new VerificationExpiredException("Сессия регистрации истекла. Начните заново.");
        }

        OtpVerificationData otpData = data.getVerification();


        Instant lastSentInstant = otpData.getLastSentAt();
        long lastSent = (lastSentInstant != null) ? lastSentInstant.getEpochSecond() : 0L;
        long now = Instant.now().getEpochSecond();

        if (now - lastSent < RESEND_COOLDOWN_SECONDS) {
            long wait = RESEND_COOLDOWN_SECONDS - (now - lastSent);
            throw new TooManyRequestsException("Повторная отправка доступна через " + wait + " сек.");
        }


        int currentResendCount = otpData.getResendCount();
        if (currentResendCount >= MAX_RESEND_ATTEMPTS) {
            redisService.delete(verificationKey);
            redisService.delete(PENDING_EMAIL_KEY + data.getUserData().getEmail());
            throw new OtpAttemptsExceededException("Превышено количество повторных отправок. Начните регистрацию заново.");
        }


        String newOtp = otpService.generateOtp();

        otpData.setOtpCode(newOtp);
        otpData.setAttempts(0);
        otpData.setLastSentAt(Instant.now());
        otpData.setResendCount(currentResendCount + 1);


        emailService.sendOtpEmail(data.getUserData().getEmail(), newOtp);


        redisService.set(verificationKey, data, OTP_TTL_SECONDS);

        log.info("Повторная отправка OTP: email={}, verificationId={}, attempt={}",
                data.getUserData().getEmail(), req.getVerificationId(), otpData.getResendCount());

        return ResendOtpResponseDto.builder()
                .message("Новый код отправлен")
                .nextResendIn(RESEND_COOLDOWN_SECONDS)
                .build();
    }

    // =============================== АВТОРИЗАЦИЯ

    @Transactional
    public LoginResponseDto loginUser(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        String email = loginRequestDto.getEmail().trim().toLowerCase();
        String password = loginRequestDto.getPassword();

        log.info("Попытка логина: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: {}", email);
                    return new InvalidCredentialsException("Неверный email или пароль");
                });


        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Неверный пароль для пользователя: {}", email);
            throw new InvalidCredentialsException("Неверный email или пароль");
        }


        if (!user.isRegisterVerify()) {
            log.warn("Попытка входа непроверенным пользователем: {}", email);
            throw new VerificationExpiredException("Email не подтверждён. Проверьте ваш почтовый ящик.");
        }


        String accessToken = jwtAccessTokenService.generateAccessToken(user);
        refreshTokenService.createRefreshToken(user, response);

        log.info("Успешный вход: {}", email);

        return LoginResponseDto.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // =============================== ВОССТАНОВЛЕНИЯ АВТОРИЗАЦИИ

    public TokenRefreshResponseDto refreshToken(HttpServletRequest request) {
        log.info("Refresh service : {}", Thread.currentThread());
        String refreshToken = refreshTokenService.extractRefreshTokenFromRequest(request)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token обязателен"));

        RefreshToken validateRefreshToken = refreshTokenService.validateRefreshToken(refreshToken);

        User user = validateRefreshToken.getUser();
        String newAccessToken = jwtAccessTokenService.generateAccessToken(user);

        return TokenRefreshResponseDto.builder()
                .username(user.getUsername())
                .accessToken(newAccessToken)
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    // =============================== ВЫХОД ИЗ АККАУНТА

    public ResponseEntity<LogoutResponseDto> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            Optional<String> tokenOpt = refreshTokenService.extractRefreshTokenFromRequest(request);

            if (tokenOpt.isEmpty()) {
                log.info("Logout запрос без refresh token - возможно пользователь уже вышел");

                refreshTokenService.clearRefreshTokenCookie(response);

                return ResponseEntity.ok()
                        .body(new LogoutResponseDto("Выход выполнен успешно"));
            }

            String refreshToken = tokenOpt.get();
            refreshTokenService.deleteRefreshToken(refreshToken);
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

    // =============================== ЗАПРОС НА СБРОС ПАРОЛЯ (ОТПРАВКА EMAIL)

    @Transactional
    public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto request) {

        String email = request.getEmail().trim().toLowerCase();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return successResponse();
        }

        User user = userOpt.get();

        if (!user.isRegisterVerify()) {
            return successResponse();
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

        return successResponse();
    }

    private ResetPasswordResponseDto successResponse() {
        return ResetPasswordResponseDto.builder()
                .success(true)
                .message("Мы отправили письмо на почту которую вы указали")
                .build();
    }

    // =============================== ПОДТВЕРЖДЕНИЕ СБРОСА ПАРОЛЯ
    public ConfirmResetPasswordResponseDto confirmResetPassword(ConfirmResetPasswordRequestDto request) {

        String newPassword = request.getNewPassword();
        String resetToken = request.getResetToken();

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(resetToken);

        if (tokenOpt.isEmpty()) {
            return ConfirmResetPasswordResponseDto.builder()
                    .success(false)
                    .message("Неверный или просроченный токен")
                    .build();
        }

        PasswordResetToken token = tokenOpt.get();

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            return ConfirmResetPasswordResponseDto.builder()
                    .success(false)
                    .message("Токен истек")
                    .build();
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return ConfirmResetPasswordResponseDto.builder()
                .success(true)
                .message("Пароль успешно обновлен")
                .build();
    }

}
