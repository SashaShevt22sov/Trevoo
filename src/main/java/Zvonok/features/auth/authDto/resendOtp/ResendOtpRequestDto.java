package Zvonok.features.auth.authDto.resendOtp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequestDto {

    @NotBlank(message = "Verification ID must not be empty")
    @Size(min = 10, max = 100, message = "Verification ID length is invalid")
    private String verificationId;
}
