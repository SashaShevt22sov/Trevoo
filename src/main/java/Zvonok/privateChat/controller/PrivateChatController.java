package Zvonok.privateChat.controller;

import Zvonok.privateChat.dto.PrivateChatCreateRequestDto;
import Zvonok.privateChat.dto.PrivateChatCreateResponseDto;
import Zvonok.privateChat.entity.PrivateChat;
import Zvonok.privateChat.service.PrivateChatService;
import Zvonok.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/private-chats")
@RequiredArgsConstructor
public class PrivateChatController {

    private final PrivateChatService privateChatService;

    @PostMapping("/create-and-get")
    public PrivateChatCreateResponseDto createChat(
            @RequestBody PrivateChatCreateRequestDto targetUserId,
            @AuthenticationPrincipal User currentUser
    ) {
        return privateChatService.createOrGetChat(targetUserId, currentUser);
    }
}
