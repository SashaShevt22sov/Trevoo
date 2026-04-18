package Zvonok.features.inviteLink.inviteLinkService;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class InviteLinkService {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateCode() {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

}
