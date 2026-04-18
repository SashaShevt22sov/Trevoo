package Zvonok.infrastructure.websocket.notification.service;


import Zvonok.features.notification.notificationDto.WebSocketDeleteNotificationDto;
import Zvonok.features.notification.notificationDto.WebSocketNotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(WebSocketNotificationResponseDto dto, String recipientUsername) {

        // ----------------- Отправка уведомлений
        messagingTemplate.convertAndSendToUser(
                recipientUsername,
                "/queue/notification",
                dto
        );
    }

        // ----------------- Удаление уведомления
    public void sendDeleteNotification(WebSocketDeleteNotificationDto dto, String recipientUsername) {
        messagingTemplate.convertAndSendToUser(
                recipientUsername,
                "/queue/notification",
                dto
        );
    }
}
