package Zvonok.privateMessage.repository;

import Zvonok.privateMessage.entity.PrivateMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivateMessageRepository extends JpaRepository<PrivateMessage,Long> {

}
