package Zvonok.friendShip.FriendShipService;

import Zvonok.common.exception.customException.friendException.CannotAddYourselfAsFriendException;
import Zvonok.common.exception.customException.friendException.FriendRequestAlreadySentException;
import Zvonok.common.exception.customException.friendException.NoPermissionException;
import Zvonok.common.exception.customException.friendException.TooManyPendingRequestsException;
import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.friendShip.FriendShipDto.FriendShipInfo;
import Zvonok.friendShip.FriendShipRepository.FriendShipRepository;
import Zvonok.friendShip.FriendShipType.FriendShipType;
import Zvonok.friendShip.entity.FriendShip;
import Zvonok.notification.notificationService.NotificationService;
import Zvonok.notification.notificationType.NotificationType;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
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
    public void addFriend(String friendUsername, Long userId) {

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

        friendShipRepository.save(fs);

        notificationService.createNotification
                (sender, friend, NotificationType.FRIEND_REQUEST,"Заявка в друзья",
                        "Хочет добавить вас в друзья");

        log.info("Новая заявка в друзья отправлена. user={}, friend={}", userId, friend.getId());
    }


    // ========================================================= Отмена заявки на дружбу
    @Transactional
    public String cancelFriendRequest(Long userId, String friendUsername) {

        User sender = getUserById(userId);
        User friendRecipient = userRepository.findByUsername(friendUsername)
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
    public String acceptFriendRequest(Long userId, String friendUsername) {
        User user = getUserById(userId);
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(UserNotFoundException::new);

        Optional<FriendShip> existing = friendShipRepository.findRelation(user, friend);

        if (existing.isEmpty() || existing.get().getStatus() != FriendShipType.PENDING) {
            throw new IllegalStateException("Заявка не найдена или уже принята");
        }

        FriendShip friendShip = existing.get();

        friendShip.setStatus(FriendShipType.ACCEPTED);
        friendShip.setUpdatedAt(now());

        friendShipRepository.save(friendShip);

        notificationService.deleteNotification(friend, user);
        return "Заявка успешно принята";
    }


    // ========================================================= Подгружаю список все заявок пользователя (PENDING)
    @Transactional
    public List<FriendShipInfo> getOutgoingRequests(Long userId) {

        User user = getUserById(userId);

        return friendShipRepository
                .findAllBySenderAndStatus(user, FriendShipType.PENDING)
                .stream()
                .map(fs -> new FriendShipInfo(
                        fs.getFriend().getId(),
                        fs.getFriend().getUsername(),
                        fs.getStatus(),
                        fs.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
    // ========================================================= Получить входящие заявки пользователя

        @Transactional
        public List<FriendShipInfo> getIncoming(Long userId) {

            User user = getUserById(userId);

            List<FriendShip> list = friendShipRepository
                    .findAllByFriendAndStatus(user, FriendShipType.PENDING);

            log.info("👉 ВХОДЯЩИЕ ЗАЯВКИ ПОЛЬЗОВАТЕЛЮ : {}", list.size());
            list.forEach(fs -> log.info("👉 {}", fs));

            return list.stream()
                    .map(fs -> new FriendShipInfo(
                            fs.getSender().getId(),
                            fs.getSender().getUsername(),
                            fs.getStatus(),
                            fs.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

    }

    // ========================================================= Получить список друзей (ACCEPT)
    @Transactional
    public List<FriendShipInfo> getFriends(Long userId) {

        User user = getUserById(userId);

        return friendShipRepository.findAllAcceptedFriends(user).stream()
                .map(fs -> {
                    User friend = fs.getSender().equals(user) ? fs.getFriend() : fs.getSender();

                    return new FriendShipInfo(
                            friend.getId(),
                            friend.getUsername(),
                            fs.getStatus(),
                            fs.getCreatedAt()
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
                .orElseThrow(UserNotFoundException::new);
    }

    private void validateNotSameUser(User user, User friend) {
        if (user.getId().equals(friend.getId())) {
            throw new CannotAddYourselfAsFriendException("Нельзя отправить заявку самому себе");
        }
    }
}
