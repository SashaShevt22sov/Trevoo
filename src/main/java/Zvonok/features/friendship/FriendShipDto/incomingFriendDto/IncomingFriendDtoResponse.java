package Zvonok.features.friendship.FriendShipDto.incomingFriendDto;

import Zvonok.features.friendship.FriendShipType.FriendShipType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomingFriendDtoResponse {
    private Long requestId;

    private Long friendId;

    private String friendUsername;

    private String friendAvatarUrl;

    private FriendShipType status;

    private LocalDateTime createdAt;
}
