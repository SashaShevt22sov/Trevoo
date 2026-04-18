package Zvonok.features.friendship.FriendShipDto.listFriendsDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListFriendsDtoResponse {

    private Long friendId;

    private String friendUsername;

    private String friendAvatarUrl;


}
