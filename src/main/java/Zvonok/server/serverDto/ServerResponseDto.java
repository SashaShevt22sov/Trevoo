package Zvonok.server.serverDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponseDto {
    private Long id;
    private String name;
    private Long ownerId;
    private String ownerUsername;
    private String inviteCode;

}
