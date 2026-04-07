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

        User user = getUserById(userId); // -- Хелп метод (Проверяю существует ли Отправитель)
        User friend = getUserByUsername(friendUsername); // -- Хелп метод (Проверяю существует ли получатель)

        validateNotSameUser(user, friend); // -- Хелп метод (Сравниваю ники что бы нельзя было добавить самого себя)

        // -- Проверяю взаимные связи
        Optional<FriendShip> existing = friendShipRepository.findRelation(user, friend);


        if (existing.isPresent()) {
            if (existing.get().getStatus() == FriendShipType.PENDING) {
                throw new FriendRequestAlreadySentException("Заявка уже отправлена");
            }
            if (existing.get().getStatus() == FriendShipType.ACCEPTED) {
                throw new FriendRequestAlreadySentException("Вы уже в друзьях");
            }
        }


        // ======= ПОДСЧЁТ И ОГРАНИЧЕНИЕ =======
        long pendingCount = friendShipRepository.countPendingByUser(user);
        if (pendingCount >= maxPendingRequests) {
            throw new TooManyPendingRequestsException(
                    "Вы достигли лимита исходящих заявок (максимум 10)"
            );
        }

        FriendShip fs = FriendShip.builder()
                .user(user)
                .friend(friend)
                .updatedAt(now())
                .createdAt(now())
                .status(FriendShipType.PENDING)
                .build();

        friendShipRepository.save(fs);

        notificationService.createNotification
                (user, friend, "Заявка в друзья",
                        "Хочет добавить вас в друзья");

        log.info("Новая заявка в друзья отправлена. user={}, friend={}", userId, friend.getId());
    }


    // ========================================================= Отмена заявки на дружбу
    @Transactional
    public String cancelFriendRequest(Long userId, String friendUsername) {

        User userSender = getUserById(userId);
        User friendRecipient = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        Optional<FriendShip> existing = friendShipRepository.findRelation(userSender, friendRecipient);

        if (existing.isEmpty() || existing.get().getStatus() != FriendShipType.PENDING) {
            throw new IllegalStateException("Заявка не найдена или уже принята");
        }

        FriendShip friendShip = existing.get();

        // Проверка что пользователь является участником заявки
        if (!friendShip.getUser().equals(userSender) && !friendShip.getFriend().equals(userSender)) {
            throw new NoPermissionException("Нет прав на выполнение действия");
        }

        friendShipRepository.delete(friendShip);

        notificationService.deleteNotification(userSender, friendRecipient);
        return "Заявка успешно отменена";
    }


    // ========================================================= Принятие заявки в друзья
    @Transactional
    public String acceptFriendRequest(Long userId, String friendUsername) {
        User user = getUserById(userId);
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

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
                .findAllByUserAndStatus(user, FriendShipType.PENDING)
                .stream()
                .map(fs -> new FriendShipInfo(
                        fs.getFriend().getId(),
                        fs.getFriend().getUsername(),
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
                    User friend = fs.getUser().equals(user) ? fs.getFriend() : fs.getUser();
                    return new FriendShipInfo(
                            fs.getFriend().getId(),
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
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с таким никнеймом не найден"));
    }

    private void validateNotSameUser(User user, User friend) {
        if (user.getId().equals(friend.getId())) {
            throw new CannotAddYourselfAsFriendException("Нельзя отправить заявку самому себе");
        }
    }
}
