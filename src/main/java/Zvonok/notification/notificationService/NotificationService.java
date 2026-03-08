package Zvonok.notification.notificationService;

import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.notification.NotificationType.NotificationType;
import Zvonok.notification.entity.Notification;
import Zvonok.notification.notificationDto.NotificationAllResponseDto;
import Zvonok.notification.notificationDto.WebSocketDeleteNotificationDto;
import Zvonok.notification.notificationDto.WebSocketNotificationResponseDto;
import Zvonok.notification.notificationRepository.NotificationRepository;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import Zvonok.websocket.controller.WebSocketNotificationController;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationController webSocketNotificationController;


    // =============================================== Создаю новое уведомление
    public void createNotification(User sender, User recipient, String title, String message) {

        Notification notification = new Notification();
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setTitle(title);
        notification.setType(NotificationType.FRIEND_REQUEST);
        notification.setRead(false);

        Notification savedNotification = notificationRepository.save(notification);

        WebSocketNotificationResponseDto response = new WebSocketNotificationResponseDto();

        response.setId(savedNotification.getId());
        response.setTitle(savedNotification.getTitle());
        response.setSenderUsername(savedNotification.getSender().getUsername());
        response.setMessage(savedNotification.getMessage());
        response.setType(savedNotification.getType().name());
        response.setTypeWebSocket("NEW");

        webSocketNotificationController.sendNotification(response, recipient.getUsername());


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

        Optional<Notification> notification = notificationRepository
                .findBySenderAndRecipientAndType(sender, recipient, NotificationType.FRIEND_REQUEST);

        if (notification.isPresent()) {
            Notification notif = notification.get();
            notificationRepository.delete(notification.get());
            WebSocketDeleteNotificationDto dtoDelete = new WebSocketDeleteNotificationDto();
            dtoDelete.setId(notif.getId());
            dtoDelete.setTypeWebSocket("DELETE");

            webSocketNotificationController.sendDeleteNotification(dtoDelete,recipient.getUsername());
        }


    }
}
