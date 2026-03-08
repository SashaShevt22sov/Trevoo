package Zvonok.otp;

import Zvonok.redis.RedisService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class OtpService {

   private final RedisService redisService;

    public OtpService( RedisService redisService) {
        this.redisService = redisService;

    }


    public String generateOtp() {

        String otpCode = String.format("%06d", new Random().nextInt(999999));

        return otpCode;
    }
}
