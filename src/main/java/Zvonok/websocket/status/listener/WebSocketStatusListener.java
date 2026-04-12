package Zvonok.websocket.status.listener;

import Zvonok.websocket.entity.connectionInfo.ConnectionInfo;
import Zvonok.websocket.status.service.WebSocketStatusService;
import Zvonok.websocket.store.WebSocketConnectionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketStatusListener {

    private final WebSocketStatusService webSocketStatusService;
    private final WebSocketConnectionStore connectionStore;
    private final TaskScheduler taskScheduler;

        @EventListener
        public void handleSessionConnected(SessionConnectedEvent event) {

            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

            if (accessor.getUser() == null) {
                return;
            }

            String username = accessor.getUser().getName();

            log.info("✅ User connected: {}", username);

            // Отправка ONLINE с небольшой задержкой
            taskScheduler.schedule(
                    () -> {
                        // проверяем, что пользователь всё ещё онлайн перед отправкой
                        if (connectionStore.isUserOnline(username)) {
                            webSocketStatusService.userOnline(username);
                            log.info("📢 Отправлено ONLINE пользователю {} после задержки", username);
                        }
                    },
                    Instant.now().plusMillis(100)
            );
        }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = accessor.getSessionId();

        ConnectionInfo info = connectionStore.getConnections().get(sessionId);

        if (info == null) {
            return;
        }

        String username = info.getUsername();

        connectionStore.removeConnection(sessionId);

        if (!connectionStore.isUserOnline(username)) {
            webSocketStatusService.userOffline(username);
        }

        log.info("❌ User disconnected: {}", username);
    }

}
