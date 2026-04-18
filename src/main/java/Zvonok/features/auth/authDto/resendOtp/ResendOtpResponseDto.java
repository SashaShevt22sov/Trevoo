package Zvonok.features.auth.authDto.resendOtp;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendOtpResponseDto {
    @NonNull
    private String message;
    private int nextResendIn;

}
