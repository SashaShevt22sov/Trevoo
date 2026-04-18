package Zvonok.features.auth.authDto.resetPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmResetPasswordRequestDto {
    @NotBlank(message = "Токен сброса не должен быть пустым")
    private String resetToken;

    @NotBlank(message = "Пароль не должен быть пустым")
    @Size(min = 8, max = 64, message = "Пароль должен быть от 8 до 64 символов")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Пароль должен содержать хотя бы одну заглавную букву, одну строчную и одну цифру"
    )
    private String newPassword;
}
