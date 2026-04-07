package Zvonok.auth.jwt.accessToken;

import Zvonok.common.exception.customException.jwtException.JwtGenerationException;
import Zvonok.common.exception.customException.jwtException.JwtSecretException;
import Zvonok.common.exception.customException.jwtException.JwtUserNotFound;
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


    // =========================================================== Генерация AccessToken
    public String generateAccessToken(User user) {

        if (user == null || user.getId() == null) {
            throw new JwtUserNotFound("User или ID не могут быть null");
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("verified", user.isRegisterVerify());

            Date issuedAt = new Date();
            Date expiresAt = new Date(System.currentTimeMillis() + jwtExpiration);

            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getUsername())
                    .setIssuedAt(issuedAt)
                    .setExpiration(expiresAt)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                    .compact();

            log.debug("Access token generated for userId={}", user.getId());
            return token;

        } catch (Exception e) {
            log.error("Ошибка генерации JWT: {}", e.getMessage());
            throw new JwtGenerationException("Ошибка генерации токена");
        }
    }

    // =========================================================== Username (subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // =========================================================== Проверка токена
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String userId = extractUsername(token);
            return userId.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("JWT невалиден: {}", e.getMessage());
            return false;
        }
    }

    // =========================================================== Expiration
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // =========================================================== Универсальный extractor
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // =========================================================== Claims
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Ошибка парсинга JWT: {}", e.getMessage());
            throw e;
        }
    }

    // =========================================================== Key
    private Key getSignInKey() {

        if (secret == null || secret.isBlank()) {
            throw new JwtSecretException("JWT secret не задан");
        }

        byte[] keyBytes = Decoders.BASE64.decode(secret.trim());

        if (keyBytes.length < 64) {
            throw new JwtSecretException(
                    "Секрет слишком короткий для HS512 (минимум 64 байта)"
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }
}