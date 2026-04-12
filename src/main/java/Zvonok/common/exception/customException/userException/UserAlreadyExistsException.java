package Zvonok.common.exception.customException.userException;

import Zvonok.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", message, HttpStatus.CONFLICT);
    }
}
