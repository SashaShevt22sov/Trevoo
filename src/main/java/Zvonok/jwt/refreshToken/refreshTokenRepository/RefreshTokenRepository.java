package Zvonok.jwt.refreshToken.refreshTokenRepository;

import Zvonok.jwt.refreshToken.entity.RefreshToken;
import Zvonok.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUser(User user);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user = :user")
    int deleteByUser(@Param("user") User user);

    boolean existsByToken(String token);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiryDate < :now")
    List<RefreshToken> findAllExpired(@Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    int deleteAllExpired(@Param("now") Instant now);

    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user = :user AND rt.revoked = false")
    long countActiveTokensByUser(@Param("user") User user);
}
