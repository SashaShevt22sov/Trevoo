package Zvonok.privateChat.repository;

import Zvonok.privateChat.entity.PrivateChat;
import Zvonok.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PrivateChatRepository extends JpaRepository<PrivateChat,Long> {

    Optional<PrivateChat> findByUser1AndUser2(User user1, User user2);

}
