package Zvonok.core.common.exception.customException.userException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class UserInvalidCredentialsException extends BaseException {
    public UserInvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message, HttpStatus.UNAUTHORIZED);
    }
}
