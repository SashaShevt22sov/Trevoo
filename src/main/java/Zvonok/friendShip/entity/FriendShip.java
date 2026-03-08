package Zvonok.friendShip.entity;

import Zvonok.friendShip.FriendShipType.FriendShipType;
import Zvonok.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "friends_ship")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendShip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();;

    @Enumerated(EnumType.STRING)
    private FriendShipType status = FriendShipType.NULL;
}
