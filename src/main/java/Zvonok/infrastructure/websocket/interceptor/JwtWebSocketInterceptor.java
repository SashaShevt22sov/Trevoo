package Zvonok.infrastructure.websocket.interceptor;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final JwtWebSocketInterceptorService jwtWebSocketInterceptorService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        String sessionId = accessor.getSessionId();
        StompCommand command = accessor.getCommand();

        log.info("=== WebSocket Interceptor [{}] ===", command);
        log.info("ID сессии: {}", sessionId);
        log.info("Время: {}", LocalDateTime.now());

        switch (command) {
            case CONNECT:
                jwtWebSocketInterceptorService.handleConnect(accessor, sessionId);
                break;
            case SUBSCRIBE:
                jwtWebSocketInterceptorService.handleSubscribe(accessor, sessionId);
                break;
            case DISCONNECT:
                jwtWebSocketInterceptorService.handleDisconnect(accessor, sessionId);
                break;
            case UNSUBSCRIBE:
                jwtWebSocketInterceptorService.handleUnsubscribe(accessor, sessionId);
            default:
                log.debug("Команда {} не требует специальной обработки", command);
        }
        log.info("=== Конец обработки WebSocket сообщения ===\n");
        return message;
    }
}
