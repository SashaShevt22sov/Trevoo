package Zvonok.websocket.interceptor;

import Zvonok.jwt.accessToken.JwtAccessTokenService;
import Zvonok.userDetails.MyUserDetailsService;

import Zvonok.websocket.store.WebSocketConnectionStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;




@Slf4j
@Service
@RequiredArgsConstructor
public class JwtWebSocketInterceptorService {

    private final WebSocketConnectionStore webSocketConnectionStore;
    private final JwtAccessTokenService jwtAccessTokenService;
    private final MyUserDetailsService myUserDetailsService;


    public void handleConnect(StompHeaderAccessor accessor, String sessionId) {
        log.info("🔌 Обработка CONNECT запроса для WebSocket подключения");

        String authHeader = accessor.getFirstNativeHeader("Authorization");
        String remoteAddress = accessor.getFirstNativeHeader("remote-address");

        log.info("Authorization header: {}", authHeader != null ? "✅ присутствует" : "❌ отсутствует");
        log.info("Remote address: {}", remoteAddress != null ? remoteAddress : "не указан");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("JWT токен отсутствует или имеет неверный формат");
            throw new IllegalArgumentException("JWT отсутствует или неверный формат");
        }

        String token = authHeader.substring(7);
        log.info("JWT токен извлечен, длина: {} символов", token.length());

        try {
            String username = jwtAccessTokenService.extractUsername(token);
            log.info("Извлечен username из токена: {}", username);

            UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
            log.info("UserDetails загружены для пользователя: {}, роли: {}",
                    username, userDetails.getAuthorities());

            if (!jwtAccessTokenService.isTokenValid(token, userDetails)) {
                log.error("JWT токен невалиден для пользователя: {}", username);
                throw new IllegalArgumentException("JWT невалиден");
            }

            log.info("JWT токен успешно валидирован для пользователя: {}", username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            accessor.setUser(authentication);
            webSocketConnectionStore.addConnection(sessionId,username);

        } catch (Exception e) {
            log.error("Ошибка при обработке JWT токена: {}", e.getMessage());
            throw new IllegalArgumentException("Ошибка валидации JWT: " + e.getMessage());
        }
    }
   public void handleSubscribe(StompHeaderAccessor accessor, String sessionId) {
        String destination = accessor.getDestination();
        String username = accessor.getUser() != null ? accessor.getUser().getName() : "anonymous";

        log.info("📋 Обработка SUBSCRIBE запроса");
        log.info("Destination: {}", destination);
        log.info("Пользователь: {}", username);

        webSocketConnectionStore.addSubscription(sessionId,destination );


        log.info("✅ Подписка на {} успешно оформлена", destination);
        log.info("📊 Всего активных подписок для сессии {}: {}",
                sessionId, webSocketConnectionStore.getUserSessions());

    }
}
