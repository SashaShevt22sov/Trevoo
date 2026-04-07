package Zvonok.privateChat.entity;

import Zvonok.privateMessage.entity.PrivateMessage;
import Zvonok.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "private_chats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;


    @Column(nullable = false)
    private LocalDateTime createdAt;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private PrivateMessage lastMessage;
}
