package Zvonok.core.common.exception.customException.accessTokenException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class AccessTokenJwtUserNotFoundException extends BaseException {
    public AccessTokenJwtUserNotFoundException(String message) {
        super("USER_NOT_FOUND_FOR_TOKEN", message, HttpStatus.UNAUTHORIZED);
    }
}
