package Zvonok.features.friendship.FriendShipDto.pendingFriendDto;

import Zvonok.features.friendship.FriendShipType.FriendShipType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PendingFriendResponse {

    private Long requestId;

    private Long friendId;

    private String friendUsername;

    private String friendAvatarUrl;

    private FriendShipType status;

    private LocalDateTime createdAt;
}
