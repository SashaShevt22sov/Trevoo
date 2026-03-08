package Zvonok.passwordResetToken.passwordResetService;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordResetTokenService {

    public String generateSecureToken() {
        byte[] bytes = new byte[32]; // 256 бит
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
