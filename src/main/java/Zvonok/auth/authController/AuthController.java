package Zvonok.auth.authController;

import Zvonok.auth.authDto.*;
import Zvonok.auth.authDto.login.LoginRequestDto;
import Zvonok.auth.authDto.login.LoginResponseDto;
import Zvonok.auth.authDto.logout.LogoutResponseDto;
import Zvonok.auth.authDto.refreshToken.TokenRefreshResponseDto;
import Zvonok.auth.authDto.resetPassword.ResetPasswordRequestDto;
import Zvonok.auth.authDto.resetPassword.ResetPasswordResponseDto;
import Zvonok.auth.authService.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =============================== РЕГИСТРАЦИЯ

    @PostMapping("/registerUser")
    public ResponseEntity<VerificationResponseDto> register(
            @RequestBody @Valid RegisterRequestDto registerRequestDto) {
        return ResponseEntity.ok(authService.registerNewUser(registerRequestDto));
    }
    // =============================== ПОДТВЕРЖДЕНИЕ РЕГИСТРАЦИИ

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

    // =============================== АВТОРИЗАЦИЯ

    @PostMapping("/loginUser")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody @Valid LoginRequestDto loginRequestDto, HttpServletResponse response) {
        return ResponseEntity.ok(authService.loginUser(loginRequestDto, response));
    }

    // =============================== ВОССТАНОВЛЕНИЯ АВТОРИЗАЦИИ

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponseDto> refresh(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    // =============================== ВЫХОД ИЗ АККАУНТА

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDto> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return authService.logout(request, response);
    }

    // =============================== ЗАПРОС НА СБРОС ПАРОЛЯ (ОТПРАВКА EMAIL)
    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request){
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // =============================== ПОДТВЕРЖДЕНИЕ СБРОСА ПАРОЛЯ


}
