package Zvonok.privateMessage.service;

import Zvonok.common.exception.customException.privateChat.PrivateChatCreateAndGetException;
import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.privateChat.entity.PrivateChat;
import Zvonok.privateChat.repository.PrivateChatRepository;
import Zvonok.privateMessage.dto.PrivateMessageNewRequestDto;
import Zvonok.privateMessage.entity.PrivateMessage;
import Zvonok.privateMessage.repository.PrivateMessageRepository;
import Zvonok.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {

    private final PrivateChatRepository privateChatRepository;
    private final PrivateMessageRepository privateMessageRepository;

    public PrivateMessage newMessage(PrivateMessageNewRequestDto request, User currentUser) {

        Long chatId = request.getChatId();
        String contentMessage = request.getContentMessage();

        PrivateChat chat = privateChatRepository.findById(chatId)
                .orElseThrow(() -> new PrivateChatCreateAndGetException("Чат не найден"));

        if (!chat.getUser1().equals(currentUser) && !chat.getUser2().equals(currentUser)) {
            throw new UserNotFoundException("Вы не можите отправлять сообщения в этот чат");
        }

        PrivateMessage message = PrivateMessage.builder()
                .chat(chat)
                .content(contentMessage)
                .createdAt(LocalDateTime.now())
                .sender(currentUser)
                .build();

        privateMessageRepository.save(message);

        chat.setLastMessage(message);
        privateChatRepository.save(chat);

        return message;
    }
}
