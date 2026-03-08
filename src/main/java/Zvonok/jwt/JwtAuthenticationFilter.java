package Zvonok.jwt;

import Zvonok.jwt.accessToken.JwtAccessTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtAccessTokenService jwtAccessTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("Security filter: " + request.getMethod() + " " + request.getRequestURI());

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.info("🔍 НАЧАЛО ОБРАБОТКИ ЗАПРОСА В JWT ФИЛЬТРЕ");
        log.info("   📍 Путь: {} {}", method, requestURI);
        log.info("   🌐 Remote IP: {}", request.getRemoteAddr());

        String jwt = extractJwtFromHeader(request);

        if (jwt != null) {
            log.info("🔑 НАЙДЕН JWT ТОКЕН В ЗАГОЛОВКЕ Authorization");

            try {
                String username = jwtAccessTokenService.extractUsername(jwt);
                log.info("   👤 Извлечен username из токена: {}", username);

                if (username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    log.info("   ✅ Username валидный, текущая аутентификация отсутствует");
                    log.info("   🔄 Пытаемся загрузить пользователя из БД: {}", username);

                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        log.info("   ✅ Пользователь загружен из БД:");
                        log.info("      👤 Username: {}", userDetails.getUsername());
                        log.info("      🛡️ Роли: {}", userDetails.getAuthorities());
                        log.info("      🔓 Аккаунт активен: {}", userDetails.isEnabled());

                        // Проверяем валидность токена
                        log.info("   🔐 Проверяем валидность токена для пользователя {}", username);
                        boolean isValid = jwtAccessTokenService.isTokenValid(jwt, userDetails);

                        if (isValid) {
                            log.info("   ✅ ТОКЕН ВАЛИДЕН! Устанавливаем аутентификацию в SecurityContext");

                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authToken.setDetails(
                                    new WebAuthenticationDetailsSource()
                                            .buildDetails(request)
                            );

                            SecurityContextHolder.getContext()
                                    .setAuthentication(authToken);

                            log.info("   🎉 Аутентификация успешно установлена для пользователя: {}", username);
                            log.info("   📋 SecurityContext содержит: {}",
                                    SecurityContextHolder.getContext().getAuthentication());
                        } else {
                            log.warn("   ⚠️ ТОКЕН НЕ ВАЛИДЕН для пользователя: {}", username);
                            log.warn("   Возможные причины: истек срок действия, неверная подпись");
                        }

                    } catch (UsernameNotFoundException e) {
                        log.error("❌ КРИТИЧЕСКАЯ ОШИБКА: Пользователь не найден в БД");
                        log.error("   👤 Username из токена: {}", username);
                        log.error("   🔍 Проверьте, существует ли пользователь с таким username");
                        log.error("   📝 Детали ошибки: {}", e.getMessage());
                        log.error("   🧹 Токен будет проигнорирован");
                    }
                } else {
                    if (username == null) {
                        log.warn("⚠️ Username в токене отсутствует (null)");
                    }
                    if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        log.info("ℹ️ Пользователь уже аутентифицирован: {}",
                                SecurityContextHolder.getContext().getAuthentication().getName());
                    }
                }

            } catch (Exception e) {
                log.error("❌ ОШИБКА ПРИ ОБРАБОТКЕ JWT ТОКЕНА");
                log.error("   Тип ошибки: {}", e.getClass().getSimpleName());
                log.error("   Сообщение: {}", e.getMessage());
                log.error("   Причина: {}", e.getCause() != null ? e.getCause().getMessage() : "неизвестна");
                log.error("   🧹 Токен будет проигнорирован, запрос продолжается как анонимный");
            }
        } else {
            log.info("ℹ️ JWT ТОКЕН ОТСУТСТВУЕТ в запросе");
            log.info("   ➡️ Запрос продолжается как анонимный (если endpoint публичный)");
        }

        log.info("➡️ ПРОПУСКАЕМ ЗАПРОС ДАЛЬШЕ ПО ЦЕПОЧКЕ ФИЛЬТРОВ");
        filterChain.doFilter(request, response);

        log.info("🏁 ЗАПРОС ОБРАБОТАН, ВЫХОД ИЗ ФИЛЬТРА");
    }

    private String extractJwtFromHeader(HttpServletRequest request) {
        log.info("📤 ИЗВЛЕЧЕНИЕ JWT ИЗ ЗАГОЛОВКА Authorization");

        String bearerToken = request.getHeader("Authorization");
        log.info("   Получен заголовок: {}", bearerToken != null ? bearerToken : "null");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.info("   ✅ JWT найден в заголовке");
            log.info("   🔑 Токен (первые 20 символов): {}...",
                    token.substring(0, Math.min(20, token.length())));
            log.info("   📏 Длина токена: {} символов", token.length());
            return token;
        }

        if (bearerToken != null) {
            log.warn("⚠️ Неверный формат заголовка Authorization. Ожидалось 'Bearer <token>', получено: '{}'",
                    bearerToken);
        } else {
            log.info("ℹ️ Заголовок Authorization отсутствует");
        }

        return null;
    }

    /**
     * Опционально: исключаем некоторые пути из фильтрации
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Проверяем, нужно ли исключить путь из фильтрации
        boolean shouldSkip = path.startsWith("/auth/register") ||
                path.startsWith("/auth/verify") ||
                path.startsWith("/auth/login") ||
                path.startsWith("/auth/logout") ||
                path.startsWith("/auth/refresh");

        if (shouldSkip) {
            log.info("⏭️ ПРОПУСКАЕМ ФИЛЬТРАЦИЮ JWT для публичного эндпоинта: {} {}", method, path);
            log.info("   ➡️ Запрос будет обработан без проверки JWT");
        }

        return shouldSkip;
    }
}