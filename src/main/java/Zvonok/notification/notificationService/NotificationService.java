package Zvonok.notification.notificationService;


import Zvonok.notification.notificationDto.NotificationsCountResponseDto;
import Zvonok.notification.notificationType.NotificationType;
import Zvonok.notification.entity.Notification;
import Zvonok.notification.notificationDto.NotificationAllResponseDto;
import Zvonok.notification.notificationDto.WebSocketDeleteNotificationDto;
import Zvonok.notification.notificationDto.WebSocketNotificationResponseDto;
import Zvonok.notification.notificationRepository.NotificationRepository;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import Zvonok.websocket.notification.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;


    // =============================================== Создаю новое уведомление
    public void createNotification(User sender, User recipient, NotificationType notificationType, String title, String message) {

        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setTitle(title);
        notification.setType(notificationType);
        notification.setRead(false);

        Notification savedNotification = notificationRepository.save(notification);

        WebSocketNotificationResponseDto response = new WebSocketNotificationResponseDto();

        response.setId(savedNotification.getId());
        response.setTitle(savedNotification.getTitle());
        response.setSenderUsername(savedNotification.getSender().getUsername());
        response.setMessage(savedNotification.getMessage());
        response.setType(savedNotification.getType().name());
        response.setTypeWebSocket("NEW");

        webSocketNotificationService.sendNotification(response, recipient.getUsername());


    }

    // =============================================== Получение количество уведомлений (Не прочитаныx)
    public Map<String, Long> getUnReadNotifications(Long userId) {
        List<NotificationsCountResponseDto> listCount = notificationRepository.countUnreadByType(userId);
        log.info("list" + listCount);
        Map<NotificationType, Long> mapCount = listCount.stream().collect(
                Collectors.toMap(
                        NotificationsCountResponseDto::getType,
                        NotificationsCountResponseDto::getCount
                )
        );
        log.info("map" + mapCount);
        long friends = mapCount.getOrDefault(NotificationType.FRIEND_REQUEST, 0L);
        long privateMessage = mapCount.getOrDefault(NotificationType.PRIVATE_MESSAGE, 0L);


        System.out.println("оличество уведомлений друкзей" + friends);
        return Map.of("friends_request", friends,
                "private_message", privateMessage);
    }


    // =============================================== Получение всех уведомлений пользователя которые (Не прочитаны)

    public List<NotificationAllResponseDto> notificationAllUsers(Long userId) {

        List<Notification> allNoRead = notificationRepository.findAllByRecipient_IdAndReadFalse(userId);

        List<NotificationAllResponseDto> dtoNotification = allNoRead.stream().map(
                notification -> NotificationAllResponseDto.builder()
                        .id(notification.getId())
                        .senderUsername(notification.getSender().getUsername())
                        .message(notification.getMessage())
                        .title(notification.getTitle())
                        .type(notification.getType().name())
                        .build()).toList();

        return dtoNotification;
    }

    // =============================================== Удаляю конкретное уведомление(FRIEND_REQUEST)

    public void deleteNotification(User sender, User recipient) {

        // ищем уведомление от sender к recipient
        notificationRepository.findBySender_IdAndRecipient_IdAndType
                        (sender.getId(), recipient.getId(), NotificationType.FRIEND_REQUEST)
                .ifPresent(notif -> {
                    notificationRepository.delete(notif);
                    sendDeleteWebSocket(notif, recipient);
                });

        // ищем уведомление от recipient к sender
        notificationRepository.findBySender_IdAndRecipient_IdAndType
                        (recipient.getId(), sender.getId(), NotificationType.FRIEND_REQUEST)
                .ifPresent(notif -> {
                    notificationRepository.delete(notif);
                    sendDeleteWebSocket(notif, sender);
                });


    }

    // =============================================== ХЕЛП МЕПТОДЫ
    private void sendDeleteWebSocket(Notification notif, User recipient) {
        WebSocketDeleteNotificationDto dtoDelete = new WebSocketDeleteNotificationDto();
        dtoDelete.setId(notif.getId());
        dtoDelete.setTypeWebSocket("DELETE");
        webSocketNotificationService.sendDeleteNotification(dtoDelete, recipient.getUsername());
    }
}
