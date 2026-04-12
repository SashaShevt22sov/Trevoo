package Zvonok.notification.notificationDto;

import Zvonok.notification.notificationType.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsCountResponseDto {

    private NotificationType type;
    private Long count;
}
