package Zvonok.jwt.refreshToken.refreshTokenDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshSilentRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
    private  String tokenType;
}
