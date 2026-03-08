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

    List<FriendShip> findAllByUser(User user);
    List<FriendShip> findAllByUserAndStatus(User user, FriendShipType status);

    boolean existsByUserAndFriend (User friend,User user);


}
