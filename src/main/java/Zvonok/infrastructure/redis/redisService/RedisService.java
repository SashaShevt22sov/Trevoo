package Zvonok.infrastructure.redis.redisService;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate,
                        StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    // String → Object

    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    // String → String

    public void setString(String key, String value, long ttlSeconds) {
      stringRedisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);

    }

    public String getString(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public void deleteString(String key) {
        stringRedisTemplate.delete(key);
    }

    public boolean existsString(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
