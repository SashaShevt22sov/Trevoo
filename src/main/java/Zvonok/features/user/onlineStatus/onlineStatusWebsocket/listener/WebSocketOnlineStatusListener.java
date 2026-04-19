package Zvonok.features.user.onlineStatus.onlineStatusWebsocket.listener;

import Zvonok.features.user.onlineStatus.onlineStatusRedisService.RedisOnlineStatusService;
import Zvonok.infrastructure.websocket.websocket.store.entity.connectionInfo.ConnectionInfo;
import Zvonok.features.user.onlineStatus.onlineStatusWebsocket.service.WebSocketOnlineStatusService;
import Zvonok.infrastructure.websocket.websocket.store.service.WebSocketConnectionStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketOnlineStatusListener {

    private final RedisOnlineStatusService redisOnlineStatusService;
    private final WebSocketOnlineStatusService webSocketStatusService;
    private final WebSocketConnectionStoreService connectionStore;

    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) {
            return;
        }

        String username = accessor.getUser().getName();

        log.info("✅ CONNECT: {}", username);

        connectionStore.addConnection(accessor.getSessionId(), username);

        redisOnlineStatusService.setOnline(username);

        webSocketStatusService.userOnline(username);

        log.info("📢 USER ONLINE: {}", username);
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        ConnectionInfo info =
                connectionStore.getConnections().get(sessionId);

        if (info == null) {
            return;
        }

        String username = info.getUsername();

        log.info("❌ DISCONNECT: {} session={}", username, sessionId);


        connectionStore.removeConnection(sessionId);

        if (!connectionStore.isUserOnline(username)) {

            redisOnlineStatusService.setOffline(username);

            webSocketStatusService.userOffline(username);

            log.info("📢 USER OFFLINE: {}", username);
        }
    }
}
