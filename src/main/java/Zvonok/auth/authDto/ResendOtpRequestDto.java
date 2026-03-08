package Zvonok.auth.authDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequestDto {
    private String verificationId;
}
