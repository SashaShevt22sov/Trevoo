package Zvonok.auth.authDto.refreshToken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRefreshResponseDto {
    private String username;
    private String accessToken;
    private String avatarUrl;
}
