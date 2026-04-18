package Zvonok.features.friendship.FriendShipDto.createRequestFriendShipDto;

import Zvonok.features.friendship.FriendShipType.FriendShipType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateRequestFriendShipResponseDto {
    private Long requestId;
    private FriendShipType requestStatus;
}
