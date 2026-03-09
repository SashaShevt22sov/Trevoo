package Zvonok.config;

import Zvonok.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        log.info("=== Инициализация SecurityFilterChain ===");

        http
                .csrf(csrf -> {
                    log.debug("CSRF protection disabled");
                    csrf.disable();
                })
                .cors(cors -> {
                    log.debug("CORS configured with custom source");
                    cors.configurationSource(corsConfigurationSource);
                })
                .sessionManagement(session -> {
                    log.debug("Session management set to STATELESS");
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .authorizeHttpRequests(auth -> {
                    log.info("Configuring HTTP authorization rules:");

                    // Логируем все открытые endpoints
                    String[] permitAllEndpoints = {
                            "/api/v1/auth/**",
                            "/avatars/**",
                            "/ws/**",
                            "/topic/**",
                            "/swagger-ui/**",
                            "/v3/**"
                    };

                    log.info("PermitAll endpoints:");
                    for (String endpoint : permitAllEndpoints) {
                        log.info("  - {}", endpoint);
                    }

                    auth.requestMatchers(permitAllEndpoints).permitAll()
                            .anyRequest().authenticated();

                    log.info("All other requests require authentication");
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("JwtAuthenticationFilter added before UsernamePasswordAuthenticationFilter");
        log.info("=== SecurityFilterChain инициализация завершена ===");

        return http.build();
    }
}
