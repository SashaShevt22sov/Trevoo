package Zvonok.features.auth.authDto;

import Zvonok.features.auth.authEntity.OtpVerificationData;
import Zvonok.features.auth.authEntity.UserRegistrationData;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRegistrationDto {
    private UserRegistrationData userData;
    private OtpVerificationData verification;
}
