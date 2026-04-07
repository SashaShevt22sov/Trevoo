package Zvonok.redis.redisService.redisRefreshTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String PREFIX = "refresh:";

    // ================= СОЗДАНИЕ =================
    public void savedRefreshToken(String token, Long userId, long ttl) {
        String key = buildKey(token);

        stringRedisTemplate.opsForValue().set(
                key, String.valueOf(userId), ttl, TimeUnit.MILLISECONDS
        );
    }

    // ================= Получить =================
    public Long getUserIdByRefreshToken(String token) {
        String key = buildKey(token);

        String value = stringRedisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        return Long.parseLong(value);
    }

    // ================= УДАЛЕНИЕ =================

    public void deleteRefreshToken(String token) {
        String key = buildKey(token);

        Boolean deletedToken = stringRedisTemplate.delete(key);

        if (Boolean.TRUE.equals(deletedToken)) {
            log.info("Рефрешь токен успешно удален: {}", token);
        } else {
            log.warn("Рефрешь токен не найден в редис для удаления : {}", token);
        }
    }

// ================= ХЕЛП МЕТОДЫ =================

    private String buildKey(String token) {
        return PREFIX + token;
    }
}
