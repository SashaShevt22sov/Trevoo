package Zvonok.server.serverController;

import Zvonok.server.serverDto.CreateServerRequestDto;
import Zvonok.server.serverDto.ServerResponseDto;
import Zvonok.server.serverService.ServerService;
import Zvonok.userDetails.MyUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @PostMapping("/create")
    public ResponseEntity<?> createServer(
            @RequestBody @Valid CreateServerRequestDto request,
            @AuthenticationPrincipal MyUserDetails userDetails) {

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