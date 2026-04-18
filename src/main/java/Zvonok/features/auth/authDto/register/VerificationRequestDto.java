package Zvonok.features.auth.authDto.register;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRequestDto {

    @NotBlank(message = "verificationId не может быть пустым")
    @Size(min = 36, max = 36, message = "verificationId должен содержать ровно 36 символов (формат UUID)")
    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "Неверный формат verificationId. Ожидается UUID (например: 123e4567-e89b-12d3-a456-426614174000)"
    )
    private String verificationId;

    @NotBlank(message = "OTP код не может быть пустым")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP код должен содержать ровно 6 цифр")
    private String otpCode;
}
