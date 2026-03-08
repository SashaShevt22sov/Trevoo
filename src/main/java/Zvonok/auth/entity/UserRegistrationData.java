package Zvonok.auth.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationData {

    private String email;
    private String username;
    private String passwordHash;

}
