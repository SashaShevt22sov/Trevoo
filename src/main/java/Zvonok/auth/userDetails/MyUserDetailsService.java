package Zvonok.auth.userDetails;

import Zvonok.user.entity.User;
import Zvonok.user.userRepository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("зашел  в UserDetails" );
        User user = userRepository.findByUsername(username)

                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        log.info("нашел usera в UserDetails"  + user);
        return new MyUserDetails(user);
    }
}
