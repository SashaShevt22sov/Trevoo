package Zvonok.features.auth.authService;


import Zvonok.features.auth.authDto.PendingRegistrationDto;
import Zvonok.features.auth.authDto.register.RegisterRequestDto;
import Zvonok.features.auth.authDto.register.RegisterResponseDto;
import Zvonok.features.auth.authDto.register.VerificationRequestDto;
import Zvonok.features.auth.authDto.register.VerificationResponseDto;
import Zvonok.features.auth.authDto.resendOtp.ResendOtpRequestDto;
import Zvonok.features.auth.authDto.resendOtp.ResendOtpResponseDto;
import Zvonok.features.auth.authEntity.OtpVerificationData;
import Zvonok.features.auth.authEntity.UserRegistrationData;
import Zvonok.core.common.Enum.Role;
import Zvonok.core.common.exception.customException.otpException.InvalidOtpCodeException;
import Zvonok.core.common.exception.customException.otpException.OtpAttemptsExceededException;
import Zvonok.core.common.exception.customException.otpException.TooManyRequestsException;
import Zvonok.core.common.exception.customException.userException.UserAlreadyExistsException;
import Zvonok.core.common.exception.customException.otpException.VerificationExpiredException;
import Zvonok.infrastructure.email.EmailService;
import Zvonok.features.auth.jwt.accessToken.JwtAccessTokenService;

import Zvonok.features.auth.jwt.refreshToken.refreshTokenService.RefreshTokenService;
import Zvonok.infrastructure.otp.OtpService;
import Zvonok.features.auth.passwordResetToken.passwordResetRepository.PasswordResetTokenRepository;
import Zvonok.features.auth.passwordResetToken.passwordResetService.PasswordResetTokenService;
import Zvonok.infrastructure.redis.redisService.RedisService;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRegisterService {


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
    private String DEFAULT_URL_AVATAR;

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
            throw new VerificationExpiredException();
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

                throw new OtpAttemptsExceededException();
            }

            throw new InvalidOtpCodeException((otpData.getMaxAttempts() - newAttempts));
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
        String newRefreshToken = refreshTokenService.createRefreshToken(savedUser, response);
        refreshTokenService.setRefreshTokenCookie(response, newRefreshToken);

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
            throw new VerificationExpiredException();
        }

        OtpVerificationData otpData = data.getVerification();

        Instant lastSentInstant = otpData.getLastSentAt();
        long lastSent = (lastSentInstant != null) ? lastSentInstant.getEpochSecond() : 0L;
        long now = Instant.now().getEpochSecond();

        if (now - lastSent < RESEND_COOLDOWN_SECONDS) {
            long wait = RESEND_COOLDOWN_SECONDS - (now - lastSent);
            throw new TooManyRequestsException(wait);
        }

        int currentResendCount = otpData.getResendCount();
        if (currentResendCount >= MAX_RESEND_ATTEMPTS) {
            redisService.delete(verificationKey);
            redisService.delete(PENDING_EMAIL_KEY + data.getUserData().getEmail());
            throw new OtpAttemptsExceededException();
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

}
