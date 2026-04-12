package Zvonok.common.exception.customException.userException;

import Zvonok.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message, HttpStatus.UNAUTHORIZED);
    }
}
