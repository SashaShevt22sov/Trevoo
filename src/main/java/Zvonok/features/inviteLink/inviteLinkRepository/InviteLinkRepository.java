package Zvonok.features.inviteLink.inviteLinkRepository;

import Zvonok.features.inviteLink.entity.InviteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteLinkRepository extends JpaRepository<InviteLink,Long> {

    boolean existsByCode(Long code);
}
