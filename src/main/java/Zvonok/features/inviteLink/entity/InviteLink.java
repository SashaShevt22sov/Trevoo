package Zvonok.features.inviteLink.entity;

import Zvonok.features.server.entity.Server;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invite_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InviteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id",nullable = false)
    private Server server;

    @Column(unique = true, nullable = false)
    private String code;


}
