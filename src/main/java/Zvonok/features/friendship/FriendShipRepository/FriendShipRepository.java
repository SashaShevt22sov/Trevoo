package Zvonok.features.friendship.FriendShipRepository;

import Zvonok.features.friendship.FriendShipType.FriendShipType;
import Zvonok.features.friendship.entity.FriendShip;
import Zvonok.features.user.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendShipRepository extends JpaRepository<FriendShip,Long> {

    // Ищю любую связь
    @Query("SELECT f FROM FriendShip f WHERE " +
            "(f.sender = :user1 AND f.friend = :user2) OR " +
            "(f.sender = :user2 AND f.friend = :user1)")
    Optional<FriendShip> findRelation(@Param("user1") User user1,
                                      @Param("user2") User user2);

    // Проверка  количество отправленных завявок у user (антиспам)
    @Query("SELECT COUNT(f) FROM FriendShip f WHERE f.sender = :user AND f.status = 'PENDING'")
    long countPendingByUser(@Param("user") User user);

    // Поиск всеx друзей ACCEPT в двух направлениях
    @Query("""
    SELECT f FROM FriendShip f
    WHERE (f.sender = :user OR f.friend = :user)
    AND f.status = 'ACCEPTED'
""")
    List<FriendShip> findAllAcceptedFriends(@Param("user") User user);
    List<FriendShip> findAllByFriendAndStatus(User friend, FriendShipType status);
    List<FriendShip> findAllBySenderAndStatus(User user, FriendShipType status);

}
