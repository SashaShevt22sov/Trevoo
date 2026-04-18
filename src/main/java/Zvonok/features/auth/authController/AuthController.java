package Zvonok.features.auth.authController;

import Zvonok.features.auth.authDto.login.LoginRequestDto;
import Zvonok.features.auth.authDto.login.LoginResponseDto;
import Zvonok.features.auth.authDto.logout.LogoutResponseDto;
import Zvonok.features.auth.authDto.refreshToken.TokenRefreshResponseDto;
import Zvonok.features.auth.authDto.register.RegisterRequestDto;
import Zvonok.features.auth.authDto.register.RegisterResponseDto;
import Zvonok.features.auth.authDto.register.VerificationRequestDto;
import Zvonok.features.auth.authDto.register.VerificationResponseDto;
import Zvonok.features.auth.authDto.resendOtp.ResendOtpRequestDto;
import Zvonok.features.auth.authDto.resendOtp.ResendOtpResponseDto;
import Zvonok.features.auth.authDto.resetPassword.ConfirmResetPasswordRequestDto;
import Zvonok.features.auth.authDto.resetPassword.ConfirmResetPasswordResponseDto;
import Zvonok.features.auth.authDto.resetPassword.ResetPasswordRequestDto;
import Zvonok.features.auth.authDto.resetPassword.ResetPasswordResponseDto;
import Zvonok.features.auth.authService.*;
import Zvonok.features.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Авторизация", description = "Управление входом и регистрацией")
public class AuthController {

    private final AuthRegisterService authRegisterService;
    private final AuthLoginService authLoginService;
    private final AuthPasswordResetService authPasswordResetService;
    private final AuthLogoutService authLogoutService;
    private final AuthRefreshService authRefreshService;

    // ===============================  РЕГИСТРАЦИИ НОВОГО ПОЛЬЗОВАТЕЛЯ
    @Operation(
            summary = "Регистрация пользователя. Отправление кода подтверждения регистрации",
            description = "Отправление кода подтверждения регистрации"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Этап регистрации пройден успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибки валидации")
    })
    @PostMapping("/registerUser")
    public ResponseEntity<VerificationResponseDto> register(
            @RequestBody @Valid RegisterRequestDto registerRequestDto) {
        return ResponseEntity.ok(authRegisterService.registerNewUser(registerRequestDto));
    }

    // ===============================  ОТПРАВКА ОТП КОДА ДЛЯ ПОДТВЕРЖДЕНИЯ РЕГИСТРАЦИИ
    @Operation(
            summary = "Подтверждение почты",
            description = "Подтверждение почты и окончательная регистрация"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Этап регистрации пройден успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибки валидации")
    })
    @PostMapping("/verifyRegister")
    public ResponseEntity<RegisterResponseDto> verifyRegister(
            @RequestBody @Valid VerificationRequestDto verificationRequestDto, HttpServletResponse response) {
        return ResponseEntity.ok(authRegisterService.verifyRegisterNewUser(verificationRequestDto, response));
    }

    // =============================== ПОВТОРНАЯ ОТПРАВКА ОТП КОДА

    @PostMapping("/resend-otp")
    public ResponseEntity<ResendOtpResponseDto> resendOtp(@RequestBody ResendOtpRequestDto resendOtpRequestDto) {
        return ResponseEntity.ok(authRegisterService.resendNewOtp(resendOtpRequestDto));
    }

    // ===============================  АВТОРИЗАЦИЯ
    @Operation(
            summary = "Получение токена",
            description = "Получение токена"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Авторизация пройдена успешно"),
            @ApiResponse(responseCode = "401", description = "Неверный email или пароль"),
            @ApiResponse(responseCode = "500", description = "Логин и пароль пустые")
    })
    @PostMapping("/loginUser")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletResponse response) {
        return ResponseEntity.ok(authLoginService.loginUser(loginRequestDto, response));
    }



    // ===============================  ВЫХОД
    @Operation(
            summary = "Выход из аккаунта",
            description = "Выход из аккаунта"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Выход выполнен успешно")
    })
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(
            HttpServletRequest request,
            HttpServletResponse response,@AuthenticationPrincipal User user
    ) {
        return authLogoutService.logout(request, response,user.getId());
    }
    // ===============================  СБРОС ПАРОЛЯ ОТПРАВКА EMAIL
    @Operation(
            summary = "Восстановление пароля",
            description = "Отправка кода восстановления"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Мы отправили письмо на почту которую вы указали")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        return ResponseEntity.ok(authPasswordResetService.resetPassword(request));
    }

    // =============================== ПОДТВЕРЖДЕНИЕ СБРОСА ПАРОЛЯ
    @PostMapping("/confirm-reset-password")
    public ResponseEntity<ConfirmResetPasswordResponseDto> confirmResetPassword(
            @RequestBody @Valid ConfirmResetPasswordRequestDto request
    ) {
        ConfirmResetPasswordResponseDto response = authPasswordResetService.confirmResetPassword(request);

        return ResponseEntity.ok(response);
    }

    // ===============================  ОБНОВЛЕНИЕ ТОКЕНА(ВОССТАНОВЛЕНИЕ AUTH)
    @Operation(
            summary = "Обновление токена",
            description = "Обновление токена"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Refresh token обязателен")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(HttpServletRequest request ,HttpServletResponse response) {
        return ResponseEntity.ok(authRefreshService.refreshToken(request,response));
    }

}
