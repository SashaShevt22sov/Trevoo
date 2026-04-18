package Zvonok.features.friendship.FriendShipService;

import Zvonok.core.common.exception.customException.friendException.*;
import Zvonok.core.common.exception.customException.userException.UserNotFoundException;
import Zvonok.features.friendship.FriendShipDto.listFriendsDto.ListFriendsDtoResponse;
import Zvonok.features.friendship.FriendShipDto.createRequestFriendShipDto.CreateRequestFriendShipResponseDto;
import Zvonok.features.friendship.FriendShipDto.incomingFriendDto.IncomingFriendDtoResponse;
import Zvonok.features.friendship.FriendShipDto.pendingFriendDto.PendingFriendResponse;
import Zvonok.features.friendship.FriendShipRepository.FriendShipRepository;
import Zvonok.features.friendship.FriendShipType.FriendShipType;
import Zvonok.features.friendship.entity.FriendShip;
import Zvonok.features.notification.notificationService.NotificationService;
import Zvonok.features.notification.notificationType.NotificationType;
import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendShipService {
    @Value("${spring.friends.maxPendingRequest}")
    private int maxPendingRequests;


    private final UserRepository userRepository;
    private final FriendShipRepository friendShipRepository;
    private final NotificationService notificationService;

    // ========================================================= ЗАЯВКА ДОБАВЛЕНИЯ В ДРУЗЬЯ
    @Transactional
    public CreateRequestFriendShipResponseDto createNewFriendRequest(String friendUsername, Long userId) {

        User sender = getUserById(userId); // -- Хелп метод (Проверяю существует ли Отправитель)
        User friend = getUserByUsername(friendUsername); // -- Хелп метод (Проверяю существует ли получатель)

        validateNotSameUser(sender, friend); // -- Хелп метод (Сравниваю ники что бы нельзя было добавить самого себя)

        // -- Проверяю взаимные связи
        Optional<FriendShip> existing = friendShipRepository.findRelation(sender, friend);


        if (existing.isPresent()) {
            if (existing.get().getStatus() == FriendShipType.PENDING) {
                throw new FriendRequestAlreadySentException("Заявка уже отправлена");
            }
            if (existing.get().getStatus() == FriendShipType.ACCEPTED) {
                throw new FriendRequestAlreadySentException("Вы уже в друзьях");
            }
        }


        // ======= ПОДСЧЁТ И ОГРАНИЧЕНИЕ =======
        long pendingCount = friendShipRepository.countPendingByUser(sender);
        if (pendingCount >= maxPendingRequests) {
            throw new TooManyPendingRequestsException(
                    "Вы достигли лимита исходящих заявок (максимум 10)"
            );
        }

        FriendShip fs = FriendShip.builder()
                .sender(sender)
                .friend(friend)
                .updatedAt(now())
                .createdAt(now())
                .status(FriendShipType.PENDING)
                .build();

        FriendShip savedFs = friendShipRepository.save(fs);

        notificationService.createNotification
                (sender, friend, NotificationType.FRIEND_REQUEST, "Заявка в друзья",
                        "Хочет добавить вас в друзья");

        log.info("Новая заявка в друзья отправлена. user={}, friend={}", userId, friend.getId());

        return new CreateRequestFriendShipResponseDto(
                savedFs.getId(),
                savedFs.getStatus()
        );
    }


    // ========================================================= Отмена заявки на дружбу
    @Transactional
    public String cancelFriendRequest(Long userId, Long friendId) {

        User sender = getUserById(userId);
        User friendRecipient = userRepository.findById(friendId)
                .orElseThrow(UserNotFoundException::new);

        Optional<FriendShip> existing = friendShipRepository.findRelation(sender, friendRecipient);

        if (existing.isEmpty() || existing.get().getStatus() != FriendShipType.PENDING) {
            throw new IllegalStateException("Заявка не найдена или уже принята");
        }

        FriendShip friendShip = existing.get();

        // Проверка что пользователь является участником заявки
        if (!friendShip.getSender().equals(sender) && !friendShip.getFriend().equals(sender)) {
            throw new NoPermissionException("Нет прав на выполнение действия");
        }

        friendShipRepository.delete(friendShip);

        notificationService.deleteNotification(sender, friendRecipient);
        return "Заявка успешно отменена";
    }


    // ========================================================= Принятие заявки в друзья
    @Transactional
    public String acceptFriendRequest(Long userId, Long requestId) {

        User user = getUserById(userId);
        FriendShip friendShip = friendShipRepository.findById(requestId)
                .orElseThrow(() -> new FriendRequestNotFoundException("Заявка не найдена"));

        if (friendShip.getStatus() != FriendShipType.PENDING) {
            throw new FriendRequestAlreadyProcessedException("Заявка уже обработана");
        }

        if (!friendShip.getFriend().getId().equals(user.getId())) {
            throw new NoPermissionException("Нету прав принять заяввку");
        }

        friendShip.setStatus(FriendShipType.ACCEPTED);
        friendShip.setUpdatedAt(now());
        friendShipRepository.save(friendShip);

        notificationService.deleteNotification(friendShip.getSender(), user);
        return "Заявка успешно принята";
    }


    // ========================================================= Подгружаю список все отправленные заявки пользователя (PENDING)
    @Transactional
    public List<PendingFriendResponse> getOutgoingRequests(Long userId) {

        User user = getUserById(userId);

        return friendShipRepository
                .findAllBySenderAndStatus(user, FriendShipType.PENDING)
                .stream()
                .map(fs -> new PendingFriendResponse(
                        fs.getId(),
                        fs.getFriend().getId(),
                        fs.getFriend().getUsername(),
                        fs.getFriend().getAvatarUrl(),
                        fs.getStatus(),
                        fs.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // ========================================================= Получить входящие заявки пользователя
    @Transactional
    public List<IncomingFriendDtoResponse> getIncoming(Long userId) {

        User user = getUserById(userId);

        List<FriendShip> list = friendShipRepository
                .findAllByFriendAndStatus(user, FriendShipType.PENDING);

        return list.stream()
                .map(fs -> new IncomingFriendDtoResponse(
                        fs.getId(),
                        fs.getSender().getId(),
                        fs.getSender().getUsername(),
                        fs.getSender().getAvatarUrl(),
                        fs.getStatus(),
                        fs.getCreatedAt()
                ))
                .collect(Collectors.toList());

    }

    // ========================================================= Получить список друзей (ACCEPT)
    @Transactional
    public List<ListFriendsDtoResponse> getFriends(Long userId) {

        User user = getUserById(userId);

        return friendShipRepository.findAllAcceptedFriends(user).stream()
                .map(fs -> {
                    boolean isSender = fs.getSender().getId().equals(user.getId());

                    User friend = isSender ? fs.getFriend() : fs.getSender();

                    return new ListFriendsDtoResponse(
                            friend.getId(),
                            friend.getUsername(),
                            friend.getAvatarUrl()
                    );
                })
                .collect(Collectors.toList());
    }

// ========================================================= ХЕЛП МЕТОДЫ

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new FriendRequestNotFoundException("Пользователя с таким username не найден"));
    }

    private void validateNotSameUser(User user, User friend) {
        if (user.getId().equals(friend.getId())) {
            throw new CannotAddSelfException("Нельзя отправить заявку самому себе");
        }
    }
}
