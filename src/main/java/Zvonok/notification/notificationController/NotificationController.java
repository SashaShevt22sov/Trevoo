package Zvonok.notification.notificationController;

import Zvonok.notification.notificationDto.NotificationAllResponseDto;
import Zvonok.notification.notificationService.NotificationService;
import Zvonok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // =============================================== Получю все уведомления пользователя которые (Не прочитаны)
    @GetMapping("/all-no-read")
    public ResponseEntity<List<NotificationAllResponseDto>> notificationAll(@AuthenticationPrincipal User userDetails) {

        Long userId = userDetails.getId();
        return ResponseEntity.ok(notificationService.notificationAllUsers(userId));

    }
    // =============================================== Количество уведомлений (Не прочитаны)
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.getUnReadNotifications(user.getId()));
    }
}
