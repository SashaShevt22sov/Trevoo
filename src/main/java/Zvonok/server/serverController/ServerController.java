package Zvonok.server.serverController;

import Zvonok.server.serverDto.CreateServerRequestDto;
import Zvonok.server.serverDto.ServerResponseDto;
import Zvonok.server.serverService.ServerService;
import Zvonok.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/servers")
@RequiredArgsConstructor
@Tag(name = "Сервера", description = "Управление серверами")
public class ServerController {

    private final ServerService serverService;

    @Operation(
            summary = "Создание сервера",
            description = "Создание сервера"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сервер создан"),
            @ApiResponse(responseCode = "403", description = "Пользователь не авторизован"),
            @ApiResponse(responseCode = "4xx", description = "Ошибка ввода со стороны пользователя")
    })
    @PostMapping("/create")
    public ResponseEntity<ServerResponseDto> createServer(
            @RequestBody @Valid CreateServerRequestDto request,
            @AuthenticationPrincipal User userDetails) {

            Long userId = userDetails.getId();
            String username = userDetails.getUsername();

            log.info("👤 Данные пользователя из контекста:");
            log.info("   ID: {}", userId);
            log.info("   Username: {}", username);

            ServerResponseDto response = serverService.createNewServer(request, userId, username);

            log.info("✅ Сервер успешно создан:");
            log.info("   ID сервера: {}", response.getId());
            log.info("   Название: {}", response.getName());
            log.info("   Владелец: {}", response.getOwnerUsername());

            log.info("=".repeat(50));
            return ResponseEntity.ok(response);
    }
}