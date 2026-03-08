package Zvonok.notification.notificationRepository;

import Zvonok.notification.NotificationType.NotificationType;
import Zvonok.notification.entity.Notification;
import Zvonok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification,Long> {

    Optional<Notification> findBySenderAndRecipientAndType(User sender, User recipient, NotificationType type);
    List<Notification> findAllByRecipient_IdAndReadFalse(Long recipientId);
}
