package Zvonok.auth.authController;

import Zvonok.auth.authDto.*;
import Zvonok.auth.authDto.login.LoginRequestDto;
import Zvonok.auth.authDto.login.LoginResponseDto;
import Zvonok.auth.authDto.logout.LogoutResponseDto;
import Zvonok.auth.authDto.refreshToken.TokenRefreshResponseDto;
import Zvonok.auth.authDto.resetPassword.ResetPasswordRequestDto;
import Zvonok.auth.authDto.resetPassword.ResetPasswordResponseDto;
import Zvonok.auth.authService.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Авторизация", description = "Управление входом и регистрацией")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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
        return ResponseEntity.ok(authService.registerNewUser(registerRequestDto));
    }

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
        return ResponseEntity.ok(authService.verifyRegisterNewUser(verificationRequestDto, response));
    }

    // =============================== ПОВТОРНАЯ ОТПРАВКА ОТП КОДА

    @PostMapping("/resend-otp")
    public ResponseEntity<ResendOtpResponseDto> resendOtp(@RequestBody ResendOtpRequestDto resendOtpRequestDto) {
        return ResponseEntity.ok(authService.resendNewOtp(resendOtpRequestDto));
    }

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
        return ResponseEntity.ok(authService.loginUser(loginRequestDto, response));
    }

    @Operation(
            summary = "Обновление токена",
            description = "Обновление токена"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Refresh token обязателен")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

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
            HttpServletResponse response
    ) {
        return authService.logout(request, response);
    }

    @Operation(
            summary = "Восстановление пароля",
            description = "Отправка кода восстановления"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Мы отправили письмо на почту которую вы указали")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // =============================== ПОДТВЕРЖДЕНИЕ СБРОСА ПАРОЛЯ


}
