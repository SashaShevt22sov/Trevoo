package Zvonok.user.userService;



import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUserByIds(List<Long> userIds){
        return userRepository.findAllById(userIds);
    }



}
