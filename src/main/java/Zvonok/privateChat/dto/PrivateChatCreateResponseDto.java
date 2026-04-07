package Zvonok.privateChat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrivateChatCreateResponseDto {
 private Long chatId;

 private Long friendId;
 private String friendUsername;

}
