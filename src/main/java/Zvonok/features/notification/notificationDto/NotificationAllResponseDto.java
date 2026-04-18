package Zvonok.features.notification.notificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationAllResponseDto {
    private  Long id;
    private String senderUsername;
    private String title;
    private String message;
    private String type;
}
