package Zvonok.auth.authDto.resetPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequestDto {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;
}
