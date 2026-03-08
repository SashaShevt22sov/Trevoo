package Zvonok.auth.authDto;

import Zvonok.auth.entity.OtpVerificationData;
import Zvonok.auth.entity.UserRegistrationData;
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
