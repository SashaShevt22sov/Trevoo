package Zvonok.infrastructure.websocket.status.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketStatusService {

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
