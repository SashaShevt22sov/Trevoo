package Zvonok.features.user.onlineStatus.onlineStatusWebsocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketOnlineStatusService {

    private final SimpMessagingTemplate messagingTemplate;

    public void userOnline(String username) {

        messagingTemplate.convertAndSend("/topic/user-online", username);

        log.info("📢 Оповещение о ONLINE пользователя: {}", username);
    }

    public void userOffline(String username) {
        messagingTemplate.convertAndSend("/topic/user-offline", username);

        log.info("📢 Оповещение о OFFLINE пользователя: {}", username);
    }
}
