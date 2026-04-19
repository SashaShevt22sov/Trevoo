package Zvonok.features.user.onlineStatus.onlineStatusRedisService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisOnlineStatusService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final Duration TTL = Duration.ofMinutes(2);
    private static final String ONLINE = "online:";

    // онлайн
    public void setOnline(String username) {
        stringRedisTemplate.opsForValue().set(key(username), "1", TTL);
    }

    // обновление активности
    public void refresh(String username) {
        stringRedisTemplate.expire(key(username), TTL);
    }

    // оффлайн
    public void setOffline(String username) {
        stringRedisTemplate.delete(key(username));
    }

    // проверка
    public boolean isOnline(String username) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(key(username))
        );
    }

    /// ====================== Хелп методы ==============================

    private String key(String username) {
        return ONLINE + username;
    }
}
