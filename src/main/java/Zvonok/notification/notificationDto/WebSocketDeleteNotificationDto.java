package Zvonok.notification.notificationDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketDeleteNotificationDto {
    private Long id;
    private String typeWebSocket;
}
