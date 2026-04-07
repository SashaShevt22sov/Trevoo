package Zvonok.privateMessage.controller;

import Zvonok.privateMessage.dto.PrivateMessageNewRequestDto;
import Zvonok.privateMessage.entity.PrivateMessage;
import Zvonok.privateMessage.service.PrivateMessageService;
import Zvonok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    @PostMapping("/new-message")
    public PrivateMessage newMessage(PrivateMessageNewRequestDto request
            , @AuthenticationPrincipal User currentUser) {
        return privateMessageService.newMessage(request, currentUser);
    }

}
