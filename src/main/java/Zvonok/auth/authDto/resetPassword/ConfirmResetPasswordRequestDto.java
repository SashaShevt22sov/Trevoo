package Zvonok.auth.authDto.resetPassword;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmResetPasswordRequestDto {
    private String resetToken;
    private String newPassword;
}
