package Zvonok.friendShip.FriendShipController;

import Zvonok.friendShip.FriendShipDto.ApiResponse;
import Zvonok.friendShip.FriendShipDto.FriendShipAddRequest;
import Zvonok.friendShip.FriendShipDto.FriendShipInfo;
import Zvonok.friendShip.FriendShipService.FriendShipService;

import Zvonok.userDetails.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/friend")
@RequiredArgsConstructor
public class FriendShipController {

    private final FriendShipService friendShipService;


    // ========================================================= Заявка на добавления в друзья
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addFriend(
            @RequestBody FriendShipAddRequest request,
            @AuthenticationPrincipal MyUserDetails currentUser) {

        Long userId = currentUser.getId();

        friendShipService.addFriend(request.getUsername(), userId);

        return ResponseEntity.ok(new ApiResponse("Заявка успешно отправлена"));
    }

    // ========================================================= Подгружаю список все заявок пользователя (PENDING)
    @GetMapping("/outgoing")
    public ResponseEntity<List<FriendShipInfo>> getOutgoingRequests(
            @AuthenticationPrincipal MyUserDetails currentUser) {

        Long userId = currentUser.getId();
        log.debug("Fetching outgoing friend requests for userId={}", userId);
        List<FriendShipInfo> outgoingRequests =
                friendShipService.getOutgoingRequests(userId);

        return ResponseEntity.ok(outgoingRequests);
    }

    // ========================================================= Подгружаю список друзей (ACCEPT)
    @GetMapping("/friends")
    public ResponseEntity<List<FriendShipInfo>> getFriends(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(friendShipService.getFriends(userDetails.getId()));
    }

    // ========================================================= Отменяю конкретную заявку
    @DeleteMapping("/cancel/{friendUsername}")
    public ResponseEntity<ApiResponse> cancelFriendRequest(
            @PathVariable String friendUsername,
            @AuthenticationPrincipal MyUserDetails currentUser
    ) {
        Long userId = currentUser.getId();

        String message = friendShipService.cancelFriendRequest(userId, friendUsername);

        return ResponseEntity.ok(new ApiResponse(message));
    }
}
