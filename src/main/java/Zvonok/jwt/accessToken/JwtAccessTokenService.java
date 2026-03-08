package Zvonok.jwt.accessToken;

import Zvonok.common.exception.customException.jwtException.JwtGenerationException;
import Zvonok.common.exception.customException.jwtException.JwtSecretException;
import Zvonok.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JwtAccessTokenService {

    @Value("${jwt.access.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long jwtExpiration;


    // Генерация AccessToken
    public String generateAccessToken(User user) {

        log.info("🚀 НАЧАЛО ГЕНЕРАЦИИ ACCESS TOKEN для пользователя: {}", user.getEmail());

        // Валидация входных данных
        if (user == null) {
            log.error("❌ ОШИБКА: Передан null вместо объекта User");
            throw new IllegalArgumentException("Пользователь не найден");
        }

        if (user.getId() == null) {
            log.error("❌ ОШИБКА: ID пользователя отсутствует. Email: {}", user.getEmail());
            throw new IllegalArgumentException("ID пользователя не найден");
        }

        if (user.getUsername() == null) {
            log.error("❌ ОШИБКА: Username отсутствует. ID пользователя: {}", user.getId());
            throw new IllegalArgumentException("Username пользователя не найден");
        }

        if (secret == null || secret.trim().isEmpty()) {
            log.error("❌ ОШИБКА: JWT секретный ключ не настроен в application.yml");
            throw new IllegalStateException("JWT пустой или нету");
        }

        log.info("📋 ДАННЫЕ ПОЛЬЗОВАТЕЛЯ:");
        log.info("   🆔 ID: {}", user.getId());
        log.info("   👤 Username: {}", user.getUsername());
        log.info("   📧 Email: {}", user.getEmail());
        log.info("   ✅ Email подтвержден: {}", user.isRegisterVerify());
        log.info("   ⏰ Время жизни токена: {} мс ({} часов/минут)",
                jwtExpiration,
                jwtExpiration / 1000 / 60 + " минут");

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("verified", user.isRegisterVerify());

        log.info("📦 Дополнительные claims в токене: userId, email, verified");

        try {
            Date issuedAt = new Date();
            Date expiresAt = new Date(System.currentTimeMillis() + jwtExpiration);

            log.info("🔧 Генерация токена...");
            log.info("   📅 Создан: {}", issuedAt);
            log.info("   ⏳ Истекает: {}", expiresAt);
            log.info("   🔐 Алгоритм подписи: HS512");

            String accessToken = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getUsername())
                    .setIssuedAt(issuedAt)
                    .setExpiration(expiresAt)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                    .compact();

            log.info("✅ УСПЕХ: AccessToken сгенерирован для пользователя: {}", user.getEmail());
            log.info("🔑 Токен (первые 20 символов): {}...",
                    accessToken.substring(0, Math.min(20, accessToken.length())));

            return accessToken;

        } catch (Exception e) {
            log.error("❌ ОШИБКА ПРИ ГЕНЕРАЦИИ ТОКЕНА для пользователя: {}", user.getEmail());
            log.error("   Причина: {}", e.getMessage());
            log.error("   Тип ошибки: {}", e.getClass().getSimpleName());
            throw new JwtGenerationException("Ошибка при генерации токена: " + e.getMessage());
        }
    }


    // Извлекаю username
    public String extractUsername(String token) {
        log.info("🔍 ИЗВЛЕЧЕНИЕ USERNAME ИЗ ТОКЕНА");
        log.info("   Токен (первые 20 символов): {}...",
                token.substring(0, Math.min(20, token.length())));

        try {
            String username = extractClaim(token, Claims::getSubject);
            log.info("✅ Извлечен username: {}", username);
            return username;
        } catch (Exception e) {
            log.error("❌ Ошибка при извлечении username из токена: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Проверка валидности токена
    public boolean isTokenValid(String token, UserDetails userDetails) {
        log.info("🔐 ПРОВЕРКА ВАЛИДНОСТИ ТОКЕНА");
        log.info("   👤 UserDetails: username={}, authorities={}",
                userDetails.getUsername(),
                userDetails.getAuthorities());

        try {
            final String username = extractUsername(token);
            log.info("   📋 Username из токена: {}", username);

            boolean usernameMatch = username.equals(userDetails.getUsername());
            log.info("   ✅ Совпадение username: {}", usernameMatch);

            boolean tokenExpired = isTokenExpired(token);
            log.info("   ⏰ Токен истек: {}", tokenExpired);

            boolean isValid = usernameMatch && !tokenExpired;
            log.info("   🏁 ИТОГ: Токен {}", isValid ? "✅ ВАЛИДЕН" : "❌ НЕ ВАЛИДЕН");

            return isValid;

        } catch (Exception e) {
            log.error("❌ Ошибка при проверке валидности токена: {}", e.getMessage());
            return false;
        }
    }

    // Проверка expiration
    private boolean isTokenExpired(String token) {
        log.info("⏰ ПРОВЕРКА СРОКА ИСТЕЧЕНИЯ ТОКЕНА");

        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();

            log.info("   ⏳ Токен истекает: {}", expiration);
            log.info("   🕐 Текущее время: {}", now);

            boolean expired = expiration.before(now);
            if (expired) {
                log.warn("⚠️ ТОКЕН ИСТЕК! Срок действия истек: {}", expiration);
            } else {
                long timeLeft = expiration.getTime() - now.getTime();
                log.info("   ✅ Токен активен. Осталось времени: {} мс ({} минут)",
                        timeLeft, timeLeft / 1000 / 60);
            }

            return expired;

        } catch (Exception e) {
            log.error("❌ Ошибка при проверке срока истечения токена: {}", e.getMessage());
            return true; // при ошибке считаем токен истекшим
        }
    }

    // Извлечь expiration
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Универсальный extractor
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        log.info("📤 ИЗВЛЕЧЕНИЕ CLAIM ИЗ ТОКЕНА");

        try {
            final Claims claims = extractAllClaims(token);
            T result = claimsResolver.apply(claims);
            log.info("   ✅ Claim успешно извлечен");
            return result;
        } catch (Exception e) {
            log.error("❌ Ошибка при извлечении claim из токена: {}", e.getMessage());
            throw e;
        }
    }

    // Получить все claims
    private Claims extractAllClaims(String token) {
        log.info("📦 ИЗВЛЕЧЕНИЕ ВСЕХ CLAIMS ИЗ ТОКЕНА");

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info("   ✅ Claims успешно извлечены");
            log.info("   📋 Содержимое claims:");
            claims.forEach((key, value) ->
                    log.info("      - {}: {}", key, value)
            );

            return claims;
        } catch (Exception e) {
            log.error("❌ Ошибка при парсинге JWT токена: {}", e.getMessage());
            throw e;
        }
    }

    private Key getSignInKey() {
        log.info("🔑 ПОЛУЧЕНИЕ КЛЮЧА ПОДПИСИ JWT");

        try {
            if (secret == null || secret.trim().isEmpty()) {
                log.error("❌ ОШИБКА: Секретный ключ не может быть пустым");
                throw new JwtSecretException("Секретный ключ не может быть пустым");
            }

            String cleanSecret = secret.trim().replaceAll("\\s+", "");
            log.info("   📏 Длина секрета (очищенного): {} символов", cleanSecret.length());

            byte[] keyBytes = Decoders.BASE64.decode(cleanSecret);
            log.info("   📏 Длина ключа в байтах: {} байт", keyBytes.length);

            if (keyBytes.length < 64) {
                log.error("❌ ОШИБКА: Секретный ключ слишком короткий для HS512");
                log.error("   Требуется минимум: 64 байта");
                log.error("   Текущая длина: {} байт", keyBytes.length);
                log.error("   Рекомендация: сгенерируйте новый ключ командой:");
                log.error("   openssl rand -base64 64");

                throw new JwtSecretException(
                        String.format("JWT секрет слишком короткий для HS512, нужен минимум 64 байта (текущая длина: %d байт)",
                                keyBytes.length));
            }

            log.info("✅ Ключ подписи успешно создан, длина соответствует требованиям HS512");
            return Keys.hmacShaKeyFor(keyBytes);

        } catch (JwtSecretException e) {
            log.error("❌ Ошибка при декодировании секретного ключа для JWT: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Неожиданная ошибка при создании ключа подписи: {}", e.getMessage());
            throw new JwtSecretException("Ошибка при создании ключа подписи: " + e.getMessage());
        }
    }
}