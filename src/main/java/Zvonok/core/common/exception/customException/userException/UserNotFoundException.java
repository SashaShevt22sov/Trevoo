package Zvonok.core.common.exception.customException.userException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException() {
        super("USER_NOT_FOUND","Пользователь не найден", HttpStatus.NOT_FOUND);
    }
}
