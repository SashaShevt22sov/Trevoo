package Zvonok.websocket.interceptor;

import Zvonok.jwt.accessToken.JwtAccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtWebSocketInterceptor implements ChannelInterceptor {

    private final JwtAccessTokenService jwtAccessTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        log.info("=== WebSocket Interceptor ===");
        log.info("Получено сообщение с командой: {}", accessor.getCommand());
        log.info("ID сессии: {}", accessor.getSessionId());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            log.info("Обработка CONNECT запроса для WebSocket подключения");

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization header: {}", authHeader != null ? "присутствует" : "отсутствует");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.error("JWT токен отсутствует или имеет неверный формат");
                throw new IllegalArgumentException("JWT отсутствует");
            }

            String token = authHeader.substring(7);
            log.info("JWT токен извлечен, длина: {} символов", token.length());
          log.info(" JWT" + token );
            String username = jwtAccessTokenService.extractUsername(token);
            log.info("Извлечен username из токена: {}", username);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
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
            log.info("Аутентификация установлена для пользователя: {}", username);
            log.info("WebSocket подключение авторизовано успешно");
        } else {
            log.debug("Команда {} не требует аутентификации, пропускаем", accessor.getCommand());
        }

        log.info("=== Конец обработки WebSocket сообщения ===\n");
        return message;
    }
}
