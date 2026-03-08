package Zvonok.auth.authDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationResponseDto {
    private String verificationId;
    private boolean response;
    private String message;
}
