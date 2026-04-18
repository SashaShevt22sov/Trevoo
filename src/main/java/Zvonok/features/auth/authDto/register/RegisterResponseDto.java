package Zvonok.features.auth.authDto.register;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterResponseDto {

    private String email;
    private String username;
    private String accessToken;
    private String avatarUrl;
}
