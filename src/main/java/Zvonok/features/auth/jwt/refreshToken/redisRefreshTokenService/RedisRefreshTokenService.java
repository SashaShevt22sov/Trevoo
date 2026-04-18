package Zvonok.features.auth.jwt.refreshToken.redisRefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String PREFIX_TOKEN = "refresh:";
    private static final String PREFIX_USER = "refresh:user:";

    // ================= СОЗДАНИЕ =================
    public void savedRefreshToken(String token, Long userId, long ttl) {
        String tokenKey = buildTokenKey(token);
        String userKey = buildUserKey(userId);

        stringRedisTemplate.opsForValue()
                .set(userKey, token, ttl, TimeUnit.MILLISECONDS);

        stringRedisTemplate.opsForValue()
                .set(tokenKey, String.valueOf(userId), ttl, TimeUnit.MILLISECONDS);
        log.info("TTL refresh (ms): {}", ttl);
    }

    // ================= Получить =================
    public Long getUserIdByRefreshToken(String token) {
        String key = buildTokenKey(token);

        String value = stringRedisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return Long.parseLong(value);
    }

    public Optional<String> getRefreshTokenByUserId(Long userId) {
        String key = buildUserKey(userId);

        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }


    // ================= УДАЛЕНИЕ =================

    public void deleteRefreshToken(String token, Long userId) {

        String tokenKey = buildTokenKey(token);
        String userKey = buildUserKey(userId);

        boolean tokenDeleted = Boolean.TRUE.equals(stringRedisTemplate.delete(tokenKey));
        boolean userDeleted = Boolean.TRUE.equals(stringRedisTemplate.delete(userKey));

        log.info("Refresh token deletion result for userId={}, tokenDeleted={}, userDeleted={}",
                userId, tokenDeleted, userDeleted);
    }

    public void deleteAllUserRefreshTokens(Long userId) {
        String userKey = buildUserKey(userId);
        String token = stringRedisTemplate.opsForValue().get(userKey);

        if (token != null) {
            stringRedisTemplate.delete(buildTokenKey(token));
        }

        stringRedisTemplate.delete(userKey);
    }

    /// ================= ХЕЛП МЕТОДЫ =================

    private String buildTokenKey(String token) {
        return PREFIX_TOKEN + token;
    }

    private String buildUserKey(Long userId) {
        return PREFIX_USER + userId;
    }
}
