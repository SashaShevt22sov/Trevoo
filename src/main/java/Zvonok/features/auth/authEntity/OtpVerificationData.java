package Zvonok.features.auth.authEntity;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationData {
    private String otpCode;
    private int attempts = 0;
    private int maxAttempts = 5;
    private Instant lastSentAt;
    private int resendCount = 0;
}
