package Zvonok.friendShip.FriendShipController;

import Zvonok.friendShip.FriendShipDto.ApiResponse;
import Zvonok.friendShip.FriendShipDto.FriendShipAddRequest;
import Zvonok.friendShip.FriendShipDto.FriendShipInfo;
import Zvonok.friendShip.FriendShipService.FriendShipService;
import Zvonok.userDetails.MyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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


    @Operation(
            summary = "Заявка на добавления в друзья", description = "Заявка на добавления в друзья"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Заявка успешно отправлена"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Пользователь с таким никнеймом не найден"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403", description = "Пользователь не авторизован"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "4xx", description = "Ошибки ввода со стороны пользователя"
            )
    })
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addFriend(
            @RequestBody @Parameter(description = "username добавляемого пользователя") FriendShipAddRequest request,
            @AuthenticationPrincipal @Parameter(hidden = true) MyUserDetails currentUser) {

        Long userId = currentUser.getId();

        friendShipService.addFriend(request.getUsername(), userId);

        return ResponseEntity.ok(new ApiResponse("Заявка успешно отправлена"));
    }

    @Operation(
            summary = "Список все заявок пользователя"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список отправленных заявок на дружбу (PENDING)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён")
    })
    @GetMapping("/outgoing")
    public ResponseEntity<List<FriendShipInfo>> getOutgoingRequests(
            @AuthenticationPrincipal @Parameter(hidden = true) MyUserDetails currentUser) {

        Long userId = currentUser.getId();
        log.debug("Fetching outgoing friend requests for userId={}", userId);
        List<FriendShipInfo> outgoingRequests =
                friendShipService.getOutgoingRequests(userId);

        return ResponseEntity.ok(outgoingRequests);
    }

    @Operation(
            summary = "Список друзей", description = "Не работает"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список друзей"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён")
    })
    @GetMapping("/friends")
    public ResponseEntity<List<FriendShipInfo>> getFriends(@AuthenticationPrincipal @Parameter(hidden = true) MyUserDetails userDetails) {
        return ResponseEntity.ok(friendShipService.getFriends(userDetails.getId()));
    }

    @Operation(
            summary = "Отклонение конкретной заявки",
            description = "Не работает!"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Заявка успешно отменена"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён!")
    })
    @DeleteMapping("/cancel/{friendUsername}")
    public ResponseEntity<ApiResponse> cancelFriendRequest(
            @PathVariable @Parameter(description = "username добавляемого пользователя") String friendUsername,
            @AuthenticationPrincipal @Parameter(hidden = true) MyUserDetails currentUser
    ) {
        Long userId = currentUser.getId();

        String message = friendShipService.cancelFriendRequest(userId, friendUsername);

        return ResponseEntity.ok(new ApiResponse(message));
    }

    // ========================================================= Принятие заявки в друзья
    @Operation(
            summary = "Принятие конкретной заявки"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Заявка успешно принята"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Доступ запрещён"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Заявка не найдена")
    })
    @PutMapping("/accept/{friendUsername}")
    public ResponseEntity<ApiResponse> acceptFriendRequest(
            @PathVariable @Parameter(description = "username добавляемого пользователя") String friendUsername,
            @AuthenticationPrincipal @Parameter(hidden = true) MyUserDetails currentUser
    ) {
        Long userId = currentUser.getId();

        String message = friendShipService.acceptFriendRequest(userId, friendUsername);

        return ResponseEntity.ok(new ApiResponse(message));
    }
}
