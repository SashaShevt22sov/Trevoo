package Zvonok.features.user.userService;



import Zvonok.features.user.entity.User;
import Zvonok.features.user.userRepository.UserRepository;
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
