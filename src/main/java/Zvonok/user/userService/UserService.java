package Zvonok.user.userService;


import Zvonok.common.exception.customException.userException.UserNotFoundException;
import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;



}
