package Zvonok.friendShip.FriendShipRepository;

import Zvonok.friendShip.FriendShipType.FriendShipType;
import Zvonok.friendShip.entity.FriendShip;
import Zvonok.user.entity.User;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FriendShipRepository extends JpaRepository<FriendShip,Long> {

    // Ищю любую связь
    @Query("SELECT f FROM FriendShip f WHERE " +
            "(f.user = :user1 AND f.friend = :user2) OR " +
            "(f.user = :user2 AND f.friend = :user1)")
    Optional<FriendShip> findRelation(@Param("user1") User user1,
                                      @Param("user2") User user2);
    // Проверка  количество отправленных завявок у user (антиспам)
    @Query("SELECT COUNT(f) FROM FriendShip f WHERE f.user = :user AND f.status = 'PENDING'")
    long countPendingByUser(@Param("user") User user);

    // Поиск всеx друзей ACCEPT в двух направлениях
    @Query("""
                SELECT f FROM FriendShip f 
                WHERE ((f.user = :user OR f.friend = :user) AND f.status = 'ACCEPTED')
            """)


    List<FriendShip> findAllAcceptedFriends(@Param("user") User user);

    List<FriendShip> findAllByUserAndStatus(User user, FriendShipType status);

    boolean existsByUserAndFriend (User friend,User user);


}
