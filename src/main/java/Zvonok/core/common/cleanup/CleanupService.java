package Zvonok.core.common.cleanup;

import Zvonok.features.auth.passwordResetToken.passwordResetRepository.PasswordResetTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CleanupService {

    private final PasswordResetTokenRepository tokenRepo;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredTokens() {
        tokenRepo.deleteAllExpired(LocalDateTime.now());

    }
}
