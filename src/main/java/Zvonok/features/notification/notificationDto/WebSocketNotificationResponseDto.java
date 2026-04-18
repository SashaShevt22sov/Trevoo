package Zvonok.features.notification.notificationDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketNotificationResponseDto {

    private Long id;
    private String senderUsername;
    private String title;
    private String message;
    private String typeWebSocket;
    private String type;
}
