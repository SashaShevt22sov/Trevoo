package Zvonok.features.notification.notificationRepository;

import Zvonok.features.notification.notificationDto.NotificationsCountResponseDto;
import Zvonok.features.notification.notificationType.NotificationType;
import Zvonok.features.notification.entity.Notification;
import Zvonok.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    // ======================= Вытаскиваю все не прочитанные уведомления , группирую по типам и возращаю количество
    @Query("""
                SELECT new Zvonok.features.notification.notificationDto.NotificationsCountResponseDto( n.type, COUNT(n))
                FROM Notification n
                WHERE n.recipient.id = :userId AND n.read = false
                GROUP BY n.type
            """)
    List<NotificationsCountResponseDto> countUnreadByType(Long userId);

    Optional<Notification> findBySenderAndRecipientAndType(User sender, User recipient, NotificationType type);
    List<Notification> findAllByRecipient_IdAndReadFalse(Long recipientId);
    Optional<Notification> findBySender_IdAndRecipient_IdAndType(Long senderId, Long recipientId, NotificationType type);
}
