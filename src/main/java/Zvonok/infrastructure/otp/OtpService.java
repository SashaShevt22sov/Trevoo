package Zvonok.infrastructure.otp;

import Zvonok.infrastructure.redis.redisService.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

   private final RedisService redisService;

    public String generateOtp() {
        String otpCode = String.format("%06d", new Random().nextInt(999999));
        return otpCode;
    }
}
