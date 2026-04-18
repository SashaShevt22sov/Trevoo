package Zvonok.core.common.exception.customException.accessTokenException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class AccessTokenJwtSecretException extends BaseException {
    public AccessTokenJwtSecretException(String message) {
        super("INVALID_ACCESS_TOKEN_SIGNATURE", message, HttpStatus.UNAUTHORIZED);
    }
}
