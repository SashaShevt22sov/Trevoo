package Zvonok.friendShip.FriendShipDto;

import Zvonok.friendShip.FriendShipType.FriendShipType;

import java.time.LocalDateTime;

public record FriendShipInfo(String friendUsername,
                             FriendShipType status,
                             LocalDateTime createdAt

                             ) {
}
