package Zvonok.auth.authDto.resetPassword;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmResetPasswordResponseDto {
    private String message;
    private boolean success;

}
