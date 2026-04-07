package Zvonok.privateMessage.dto;

import lombok.Data;

@Data
public class PrivateMessageNewRequestDto {
    Long chatId;
    String contentMessage;
}
