package Zvonok.features.friendship.FriendShipController;

import Zvonok.features.friendship.FriendShipDto.ApiResponse;
import Zvonok.features.friendship.FriendShipDto.listFriendsDto.ListFriendsDtoResponse;
import Zvonok.features.friendship.FriendShipDto.createRequestFriendShipDto.CreateRequestFriendShipRequestDto;
import Zvonok.features.friendship.FriendShipDto.createRequestFriendShipDto.CreateRequestFriendShipResponseDto;
import Zvonok.features.friendship.FriendShipDto.incomingFriendDto.IncomingFriendDtoResponse;
import Zvonok.features.friendship.FriendShipDto.pendingFriendDto.PendingFriendResponse;
import Zvonok.features.friendship.FriendShipService.FriendShipService;
import Zvonok.features.user.entity.User;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Друзья", description = "Работа с сервисом \"Друзья\"")
public class FriendShipController {

    private final FriendShipService friendShipService;

    // ========================================================= Заявка на добавление в друзья
    @PostMapping("/add")
    public ResponseEntity<CreateRequestFriendShipResponseDto> createFriendRequest(
            @Valid @RequestBody @Parameter(description = "username добавляемого пользователя") CreateRequestFriendShipRequestDto request,
            @AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        Long userId = currentUser.getId();
        return ResponseEntity.ok(friendShipService.createNewFriendRequest(request.getFriendUsername(), userId));
    }

    // ========================================================= Список входящих заявок пользователю
    @GetMapping("/incoming")
    public ResponseEntity<List<IncomingFriendDtoResponse>> getIncomingRequest(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(friendShipService.getIncoming(user.getId()));
    }

    // ========================================================= Список отправленных заявок пользователем
    @GetMapping("/outgoing")
    public ResponseEntity<List<PendingFriendResponse>> getOutgoingRequests(
            @AuthenticationPrincipal @Parameter(hidden = true) User currentUser) {
        Long userId = currentUser.getId();
        log.debug("Fetching outgoing friend requests for userId={}", userId);
        return ResponseEntity.ok(friendShipService.getOutgoingRequests(userId));
    }

    // ========================================================= Подгружаю всех друзей(Accept)
    @GetMapping("/friends")
    public ResponseEntity<List<ListFriendsDtoResponse>> getFriends(@AuthenticationPrincipal @Parameter(hidden = true) User userDetails) {
        return ResponseEntity.ok(friendShipService.getFriends(userDetails.getId()));
    }

    // ========================================================= Отмена заявки(Sender)
    @DeleteMapping("/cancel/{requestId}")
    public ResponseEntity<ApiResponse> cancelFriendRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser
    ) {
        Long userId = currentUser.getId();
        String message = friendShipService.cancelFriendRequest(userId, requestId);
        return ResponseEntity.ok(new ApiResponse(message));
    }

    // ========================================================= Принятие заявки в друзья
    @PutMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse> acceptFriendRequest(
            @PathVariable @Parameter Long requestId,
            @AuthenticationPrincipal @Parameter(hidden = true) User currentUser
    ) {
        Long userId = currentUser.getId();
        String message = friendShipService.acceptFriendRequest(userId, requestId);
        return ResponseEntity.ok(new ApiResponse(message));
    }
}
