package Zvonok.server.serverService;

import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.inviteLink.entity.InviteLink;
import Zvonok.inviteLink.inviteLinkService.InviteLinkService;
import Zvonok.server.entity.Server;
import Zvonok.server.serverDto.CreateServerRequestDto;
import Zvonok.server.serverDto.ServerResponseDto;
import Zvonok.server.serverRepository.ServerRepository;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
@RequiredArgsConstructor
public class ServerService {

    private final UserRepository userRepository;
    private final ServerRepository serverRepository;
    private final InviteLinkService inviteLinkService;

    @Transactional
    public ServerResponseDto createNewServer(CreateServerRequestDto request, Long userId, String username) {

        log.info("=".repeat(50));
        log.info("🏗️ НАЧАЛО СОЗДАНИЯ СЕРВЕРА");

        try {
            if (userId == null) {
                throw new IllegalArgumentException("ID пользователя не может быть null");
            }

            boolean userExists = userRepository.existsById(userId);
            log.info("   Пользователь {} в БД", userExists ? "✅ НАЙДЕН" : "❌ НЕ НАЙДЕН");

            if (!userExists) {
                log.error("❌ ОШИБКА: Пользователь с ID {} не найден в БД", userId);
                throw new UserNotFoundException();
            }

            log.info("   👤 Получаю ссылку на пользователя (прокси)");
            User owner = userRepository.getReferenceById(userId);

            log.info("   🏗️ Создаю объект Server");
            Server server = Server.builder()
                    .name(request.getName())
                    .owner(owner)
                    .build();
            log.info("   ✅ Server объект создан: {}", server);

            log.info("   🔑 Генерирую inviteCode");
            String inviteCode = inviteLinkService.generateCode();
            log.info("   ✅ inviteCode сгенерирован: {}", inviteCode);

            log.info("   🔗 Создаю InviteLink");
            InviteLink inviteLink = InviteLink.builder()
                    .code(inviteCode)
                    .server(server)
                    .build();
            log.info("   ✅ InviteLink создан: {}", inviteLink);




            log.info("   📥 Добавляю InviteLink в коллекцию сервера");
            server.getInvites().add(inviteLink);
            log.info("   ✅ InviteLink добавлен, размер коллекции: {}", server.getInvites().size());

            log.info("   💾 Сохраняю сервер в БД");
            Server savedServer = serverRepository.save(server);
            log.info("   ✅ Сервер сохранен с ID: {}", savedServer.getId());

            log.info("   📦 Формирую ответ DTO");
            ServerResponseDto response = ServerResponseDto.builder()
                    .id(savedServer.getId())
                    .ownerId(userId)
                    .name(savedServer.getName())
                    .ownerUsername(username)
                    .inviteCode(inviteCode)
                    .build();

            log.info("✅ СЕРВЕР УСПЕШНО СОЗДАН:");
            log.info("   ID: {}", response.getId());
            log.info("   Название: {}", response.getName());
            log.info("   Владелец: {} (ID: {})", response.getOwnerUsername(), response.getOwnerId());
            log.info("   InviteCode: {}", response.getInviteCode());

            return response;

        } catch (Exception e) {
            log.error("❌ ОШИБКА ПРИ СОЗДАНИИ СЕРВЕРА:", e);
            log.error("   Тип: {}", e.getClass().getSimpleName());
            log.error("   Сообщение: {}", e.getMessage());
            throw e; // пробрасываем дальше
        }
    }
}