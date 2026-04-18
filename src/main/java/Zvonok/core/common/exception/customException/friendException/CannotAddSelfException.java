package Zvonok.core.common.exception.customException.friendException;

import Zvonok.core.common.exception.customException.BaseException;
import org.springframework.http.HttpStatus;

public class CannotAddSelfException extends BaseException {
    public CannotAddSelfException(String message) {
        super("CANNOT_ADD_SELF", message, HttpStatus.BAD_REQUEST);
    }
}
