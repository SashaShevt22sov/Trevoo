package Zvonok.privateChat.service;

import Zvonok.common.exception.customException.privateChat.PrivateChatCreateAndGetException;
import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.privateChat.dto.PrivateChatCreateRequestDto;
import Zvonok.privateChat.dto.PrivateChatCreateResponseDto;
import Zvonok.privateChat.entity.PrivateChat;
import Zvonok.privateChat.repository.PrivateChatRepository;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateChatService {

    private final UserRepository userRepository;
    private final PrivateChatRepository privateChatRepository;

    public PrivateChatCreateResponseDto createOrGetChat(PrivateChatCreateRequestDto targetUserId, User currentUser) {


        User targetUser = userRepository.findById(targetUserId.getTargetUserId())
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (currentUser.getId().equals(targetUserId.getTargetUserId())) {
            throw new PrivateChatCreateAndGetException("Нельзя написать самому себе ");

        }

        Optional<PrivateChat> existingChat = privateChatRepository.findByUser1AndUser2(currentUser, targetUser);

        if (existingChat.isEmpty()) {
            existingChat = privateChatRepository.findByUser1AndUser2(targetUser, currentUser);
        }

        if (existingChat.isPresent()) {
            PrivateChat chat = existingChat.get();

            User companion = chat.getUser1().equals(currentUser) ? chat.getUser2() : chat.getUser1();

            return PrivateChatCreateResponseDto.builder()
                    .chatId(chat.getId())
                    .friendId(companion.getId())
                    .friendUsername(companion.getUsername())
                    .build();

        }


        User user1 = currentUser.getId() < targetUser.getId() ? currentUser : targetUser;
        User user2 = currentUser.getId() < targetUser.getId() ? targetUser : currentUser;

        PrivateChat chat = PrivateChat.builder()
                .user1(user1)
                .user2(user2)
                .createdAt(LocalDateTime.now())
                .build();

        privateChatRepository.save(chat);

        User companion = chat.getUser1().equals(currentUser) ? chat.getUser2() : chat.getUser1();

        return PrivateChatCreateResponseDto.builder()
                .chatId(chat.getId())
                .friendId(companion.getId())
                .friendUsername(companion.getUsername())
                .build();
    }
}
